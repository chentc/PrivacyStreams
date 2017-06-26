package com.github.privacystreams.communication;


import android.Manifest;
import android.database.ContentObserver;

import com.github.privacystreams.core.UQI;
import com.github.privacystreams.core.exceptions.PSException;
import com.github.privacystreams.core.providers.MStreamProvider;
import com.github.privacystreams.core.purposes.Purpose;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.ContactsContract.Contacts;


/**
 * Provide a stream of updated contacts
 */

public class ContactUpdatesProvider extends MStreamProvider {
    private Contact lastUpdatedContact = null;
    private ContactStateObserver contactStateObserver;
    private List contactList = null;
    private long lastUpdateTime = 0;

    public ContactUpdatesProvider() {
        this.addRequiredPermissions(Manifest.permission.READ_CONTACTS);
    }


    @Override
    protected void provide() {
        contactStateObserver = new ContactStateObserver();
        UQI uqi = new UQI(getContext());
        try {
            contactList = uqi.getData(Contact.getAll(), Purpose.FEATURE("get original contacts on phone")).asList();
        } catch (PSException e) {
            e.printStackTrace();
        }
        uqi.stopAll();
        getContext().getContentResolver().registerContentObserver(Contacts.CONTENT_URI, false, contactStateObserver);
    }

    //observer
    private class ContactStateObserver extends ContentObserver {

        ContactStateObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            //import Date to handle exceptions raised by actions like refreshing the contact
            //which will call the onChange method but will return nothing
            Date timer = new Date();
            long thisTime = timer.getTime();
            if (thisTime - lastUpdateTime > 1000) {
                UQI uqi = new UQI(getContext());
                List newContactList;
                Contact newContactUpdateOutput;

                try {
                    newContactList = uqi.getData(Contact.getAll(),
                            Purpose.FEATURE("to get the new contact list")).asList();
                uqi.stopAll();
                    List<Contact> oldContactList = new ArrayList<>();
                    //get a deep copy of contact list to avoid bugs happening during the delete part
                    for (Object o: contactList
                         ) {oldContactList.add(new Contact((Contact) o));}
                    newContactUpdateOutput = contactChange(oldContactList, newContactList);

                    if (newContactUpdateOutput!=null
                            &&!newContactUpdateOutput.equals(lastUpdatedContact)) {
                        ContactUpdatesProvider.this.output(newContactUpdateOutput);
                        lastUpdatedContact = newContactUpdateOutput;
                    }
                } catch (PSException e) {
                    e.printStackTrace();
                }
            }
            lastUpdateTime = thisTime;
            super.onChange(selfChange);
        }
    }

    @Override
    public void onCancel(UQI uqi) {
        getContext().getContentResolver().unregisterContentObserver(contactStateObserver);
    }

    /**
     * this method takes two list of contact in which only one contact differs
     * this method will return the changed element, including three types: add, delete and edit
     * @param oldContactList contact list before the onchange method is called
     * @param newContactList new contact list after the change
     * @return editedContact
     * @throws PSException exceptions caused by casting
     */
    private Contact contactChange(List<Contact> oldContactList, List newContactList) throws PSException {
        Contact editedContact;
        List<Long> listOfID = new ArrayList<>();
        List<Long> newListOfID = new ArrayList<>();
        int listTotal = oldContactList.size();
        int newListTotal = newContactList.size();
        for (int i = 0; i < listTotal; i++) {
            Contact contactAti = oldContactList.get(i);
            listOfID.add((long) contactAti.getValueByField(Contact.ID));
        }
        for (int j = 0; j < newListTotal; j++) {
            Contact newContactAtj = (Contact) newContactList.get(j);
            newListOfID.add((long) newContactAtj.getValueByField(Contact.ID));
        }

        List<Long> intersection = new ArrayList<>();
        for (int k = 0; k < listOfID.size(); k++) {
            intersection.add(listOfID.get(k));
        }
        intersection.retainAll(newListOfID);

        //add
        if (intersection.size() != newListOfID.size()&&intersection.size()==listOfID.size()) {
            Contact tempContact = (Contact) newContactList.get(newContactList.size() - 1);
            tempContact.setFieldValue(Contact.STATUS, "added");
            editedContact = tempContact;
            contactList = newContactList;
            return editedContact;
        }
        //delete
        else if (intersection.size() != listOfID.size()&&intersection.size()==newListOfID.size()) {
            List<Long> difference = new ArrayList<>();
            for (int j = 0; j < listTotal; j++) {
                difference.add(listOfID.get(j));
            }
            difference.removeAll(intersection);
            long deleted = difference.get(0);
            for (int i = 0; i < listTotal; i++) {
                if (deleted == listOfID.get(i)) {
                    Contact tempContact = oldContactList.get(i);
                    tempContact.setFieldValue(Contact.STATUS, "deleted");
                    editedContact = tempContact;
                    contactList = newContactList;
                    return editedContact;
                }
            }
        }
        //edit
        else {
            int i = 0;
            do {
                long newTimeCreated = ((Contact) newContactList.get(i)).getValueByField(Contact.TIME_CREATED);
                Contact oldContact = oldContactList.get(i);
                oldContact.setFieldValue(Contact.TIME_CREATED, newTimeCreated);
                if (!(oldContactList.get(i)).equals((Contact) newContactList.get(i))) {
                    Contact tempContact = (Contact) newContactList.get(i);
                    tempContact.setFieldValue(Contact.STATUS, "edited");
                    editedContact = tempContact;
                    contactList = newContactList;
                    return editedContact;
                }
                i++;
            } while (i < contactList.size());
        }
        contactList = newContactList;
        return null;
    }
}



