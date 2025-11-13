package com.example.commondto.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;

public abstract class KafkaTopics {
    public static final String USER_VALIDATION_REQUEST = "user-validation-request";
    public static final String USER_VALIDATION_RESPONSE = "user-validation-response";
    public static final String WALLET_CREATION_REQUEST = "wallet-creation-request";
    public static final String WALLET_CREATION_RESPONSE = "wallet-creation-response";
    public static final String VERIFY_CREATION_REQUEST = "verify-creation-request";
    public static final String VERIFY_CREATION_RESPONSE = "verify-creation-response";
    public static final String VERIFY_UPDATE_MESSAGE = "verify-update-message";
    public static final String CC_UPDATE_MESSAGE = "cc-update-message";
    public static final String CC_VALIDATE_REQUEST = "cc-validate-request";
    public static final String CC_VALIDATE_RESPONSE = "cc-validate-response";


    public static final String MARKET_PURCHASE_EVENT = "market-purchase-event";
    public static final String MARKET_PAYMENT_EVENT = "market-payment-event";
    public static final String MARKET_PAYMENT_REQUEST = "market-payment-request";
    public static final String MARKET_PAYMENT_RESPONSE = "market-payment-response";
}
