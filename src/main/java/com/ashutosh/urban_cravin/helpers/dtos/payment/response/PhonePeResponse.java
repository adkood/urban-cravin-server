//package com.ashutosh.urban_cravin.helpers.dtos.payment.response;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Data;
//
//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class PhonePeResponse {
//    private boolean success;
//    private String code;
//    private String message;
//    private PhonePeData data;
//}
//
//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
//class PhonePeData {
//    private String merchantId;
//    private String merchantTransactionId;
//    private String transactionId;
//
//    @JsonProperty("instrumentResponse")
//    private InstrumentResponse instrumentResponse;
//}
//
//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
//class InstrumentResponse {
//    private String type;
//    private String redirectInfo;
//
//    @JsonProperty("redirectInfo")
//    private void unpackRedirectInfo(String redirectInfo) {
//        this.redirectInfo = redirectInfo;
//    }
//
//    public String getUrl() {
//        if (redirectInfo != null && redirectInfo.contains("url=")) {
//            return redirectInfo.substring(redirectInfo.indexOf("url=") + 4);
//        }
//        return redirectInfo;
//    }
//}


package com.ashutosh.urban_cravin.helpers.dtos.payment.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhonePeResponse {
    private boolean success;
    private String code;
    private String message;
    private PhonePeData data;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class PhonePeData {
    private String merchantId;
    private String merchantTransactionId;
    private String transactionId;
    private Long amount;
    private String state;
    private String responseCode;

    @JsonProperty("instrumentResponse")
    private InstrumentResponse instrumentResponse;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class InstrumentResponse {
    private String type;

    @JsonProperty("redirectInfo")
    private RedirectInfo redirectInfo;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class RedirectInfo {
    private String url;
    private String method;
}