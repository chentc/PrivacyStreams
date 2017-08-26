package io.github.privacystreams.communication.emailinfo;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.privacystreams.communication.EmailInfoProvider;
import io.github.privacystreams.core.PStreamProvider;
import io.github.privacystreams.utils.Logging;

public class OnlineOrderProvider extends EmailInfoProvider {
    private static final String domain = "order";

    public OnlineOrderProvider(String api_key, String api_secret){
        super(api_key,api_secret,domain);
    }

    @Override
    protected void provide() {
        super.provide();
    }

    @Override
    public void isSiftAvailable(JsonNode jsonNode){
        getOrder(jsonNode);
    }

    private void getOrder(JsonNode jsonNode){
        Logging.error("a new order");
        Order order = new Order();
        order.setFieldValue(Order.ACCEPTED_OFFER,jsonNode.get("acceptedOffer"));
        order.setFieldValue(Order.DESCRIPTION,jsonNode.get("description").toString());
        order.setFieldValue(Order.ORDER_NUMBER,jsonNode.get("orderNumber").toString());
        order.setFieldValue(Order.PART_OF_INVOICE,jsonNode.get("partOfInvoice"));
        order.setFieldValue(Order.SELLER,jsonNode.get("seller"));
        order.setFieldValue(Order.BROKER,jsonNode.get("broker"));
        order.setFieldValue(Order.PAYMENT_SCHEDULED,jsonNode.get("x-paymentScheduled").toString());
        order.setFieldValue(Order.PRICE,jsonNode.get("x-price").toString());
        order.setFieldValue(Order.SUBTOTAL,jsonNode.get("x-subTotal").toString());
        order.setFieldValue(Order.REFUND,jsonNode.get("x-refund").toString());
        output(order);
    }


}
