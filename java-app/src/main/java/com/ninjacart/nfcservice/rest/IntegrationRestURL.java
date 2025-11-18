package com.ninjacart.nfcservice.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IntegrationRestURL {

  @Autowired private BaseURLConfiguration baseURLConfiguration;

  private static String SEND_MATRIX_MESSAGE_URL_PREFIX = "/_matrix/client/r0/rooms";
  private static String SEND_MATRIX_MESSAGE_URL_SUFFIX = "/send/m.room.message";
  private static String MATRIX_ROOM_CREATION_URL = "/_matrix/client/r0/createRoom";
  private static String JOIN_USER_TO_ROOM_URL = "/_synapse/admin/v1/join";
  private static String MATRIX_USER_ONBOARD_URL = "/_synapse/admin/v1/register";
  private static String MATRIX_NONCE_URL = "/_synapse/admin/v1/register";
  private static String REQUEST_GROUP_CREATION_URL = "/requestGroup";

  public static String SAVE_MEMBERS_OF_THE_ROOM_URL = "/freeFlow/party";
  // TODO: confirm
  private static String REQUEST_CREATION_URL = "/v1/execution/service/run/nfc-request-creation-v2";
  // TODO: confirm
  private static String TENANT_REQUEST_CREATION_URL = "/nfc/request";


  private static String NFC_BROADCAST_SEARCH_URL = "/v1/execution/service/run/nfc-broadcast-search-request";
  private static String USER_NAME_AVAILABILITY_URL = "/_matrix/client/r0/register/available";
  private static String MATRIX_LOGIN_API = "/_matrix/client/api/v1/login";

  private static String MATRIX_EVENT_FETCH_URL_PREFIX = "/_matrix/client/v3/rooms";
  private static String MATRIX_EVENT_FETCH_URL_SUFFIX = "/event";
  private static String UPDATE_REQUEST_STATUS_URL = "/freeFlow/v2/status/update";
  private static String UPDATE_CONSENT_STATUS = "/consents/createOrUpdate";
  private static String FETCH_REQUEST_URL = "/freeFlow/filter/flat/v1";
  private static String ORDER_UPDATE_STATUS = "/freeFlow/v3/status/update";
  private static String ORDER_REQUEST_CREATION_URL =
      "/v1/execution/service/run/nfc-order-request-creation";
  private static String ORDER_REQUEST_APPROVAL_URL =
          "/v1/execution/service/run/nfc-order-approval";
  private static String FETCH_CONSENTS_URL =
          "/v1/execution/service/run/nfc-fetch-consents";
  private static String USER_STORE_ADVANCE_SEARCH_URL="/v1/execution/service/run/uss-advancedSearch";
  private static String GEMS_PREFIX_URL =
          "/api/v1/realm";
  private static String GEMS_USER_PREFIX_URL =
          "/user";

  private String ADVANCED_SEARCH_PREFIX="advancedSearch";
  private static String KICK_USER_FROM_ROOM_URL_PREFIX = "/_matrix/client/v3/rooms";
  private static String KICK_USER_FROM_ROOM_URL_SUFFIX = "/kick";
  private static String ELASTIC_SEARCH_SUFFIX="_search";
  private static String CHECK_USER = "/_synapse/admin/v2/users/";
  private static String NFC_CHAT_URL_PREFIX = "/chat?roomId=";
  private static String NFC_CHAT_URL_SUFFIX = ":qa-nfc.ninjacart.in&showSummary=false" ;
  private static String OAUTH_URL = "/iam/oauth2/token";

  public String sendMessageUrl(String roomId, String uniqueTransactionId) {
    return String.format(
        "%s%s/%s%s/%s",
        baseURLConfiguration.getMatrixBaseUrl(),
        SEND_MATRIX_MESSAGE_URL_PREFIX,
        roomId,
        SEND_MATRIX_MESSAGE_URL_SUFFIX,
        uniqueTransactionId);
  }


  public String roomCreationUrl() {
    return String.format("%s%s", baseURLConfiguration.getMatrixBaseUrl(), MATRIX_ROOM_CREATION_URL);
  }

  public String getJoinUserToRoomUrl(String roomId) {
    return String.format(
        "%s%s/%s", baseURLConfiguration.getMatrixBaseUrl(), JOIN_USER_TO_ROOM_URL, roomId);
  }

  public String getMatrixUserOnboardUrl() {
    return String.format("%s%s", baseURLConfiguration.getMatrixBaseUrl(), MATRIX_USER_ONBOARD_URL);
  }

  public String getMatrixNonceUrl() {
    return String.format("%s%s", baseURLConfiguration.getMatrixBaseUrl(), MATRIX_NONCE_URL);
  }

  public String getRequestGroupCreationUrl(String realmId, Integer userId) {
    return String.format(
        "%s/%s/%s%s",
        baseURLConfiguration.getOrderManagementBaseUrl(),
        realmId,
        userId,
        REQUEST_GROUP_CREATION_URL);
  }
  public String saveMembersOfTheRoomUrl(String realmId, Integer userId ){
    return String.format(
            "%s/%s/%s%s",
            baseURLConfiguration.getOrderManagementBaseUrl(),
            realmId,
            userId,
            SAVE_MEMBERS_OF_THE_ROOM_URL);

  }

  public String getRequestCreationUrl(String realmId, Integer userId) {
    return String.format(
        "%s/%s/%s%s",
        baseURLConfiguration.getWorkflowBaseUrl(), realmId, userId, REQUEST_CREATION_URL);
  }

  //  public String getTenantRequestCreationUrl(String tenantBaseUrl, String realmId, Integer
  // userId) {
  //    return String.format("%s/%s/%s%s", tenantBaseUrl, realmId, userId,
  // TENANT_REQUEST_CREATION_URL);
  //  }

  public String getTenantRequestCreationUrl(String tenantBaseUrl) {
    return String.format("%s%s", tenantBaseUrl, TENANT_REQUEST_CREATION_URL);
  }

  public String getNfcForwardSearchUrl(String realmId, Integer userId) {
    return String.format(
        "%s/%s/%s%s",
        baseURLConfiguration.getWorkflowBaseUrl(), realmId, userId, NFC_BROADCAST_SEARCH_URL);
  }

  public String getEventByIdAndRoomId(String roomId, String eventId) {
    return String.format(
        "%s%s/%s%s/%s",
        baseURLConfiguration.getMatrixBaseUrl(),
        MATRIX_EVENT_FETCH_URL_PREFIX,
        roomId,
        MATRIX_EVENT_FETCH_URL_SUFFIX,
        eventId);
  }

  public String getUpdateRequestStatusUrl(String realmId, Integer userId) {
    return String.format(
        "%s/%s/%s%s",
        baseURLConfiguration.getOrderManagementBaseUrl(),
        realmId,
        userId,
        UPDATE_REQUEST_STATUS_URL);
  }

  public String getUpdateConsentStatusUrl(String realmId, Integer userId) {
    return String.format(
        "%s/%s/%s%s",
        baseURLConfiguration.getOrderManagementBaseUrl(), realmId, userId, UPDATE_CONSENT_STATUS);
  }

  public String getFetchRequestUrl(String realmId, Integer userId) {
    return String.format(
        "%s/%s/%s%s",
        baseURLConfiguration.getOrderManagementBaseUrl(), realmId, userId, FETCH_REQUEST_URL);
  }
  public String getUpdateStatusUrl(String realmId, Integer userId) {
    return String.format(
        "%s/%s/%s%s",
        baseURLConfiguration.getOrderManagementBaseUrl(), realmId, userId, ORDER_UPDATE_STATUS);
  }
  public String getOrderRequestCreationUrl(String realmId, Integer userId) {
    return String.format(
        "%s/%s/%s%s",
        baseURLConfiguration.getWorkflowBaseUrl(), realmId, userId, ORDER_REQUEST_CREATION_URL);
  }

  public String getOrderRequestApprovalUrl(String realmId, Integer userId) {
    return String.format(
            "%s/%s/%s%s",
            baseURLConfiguration.getWorkflowBaseUrl(), realmId, userId, ORDER_REQUEST_APPROVAL_URL);
  }
  public String getFetchConsentsUrl(String realmId, Integer userId) {
    return String.format(
            "%s/%s/%s%s",
            baseURLConfiguration.getWorkflowBaseUrl(), realmId, userId, FETCH_CONSENTS_URL);
  }
  public String getUserStoreSearchUrl(String realmId, Integer userId) {
    return String.format(
        "%s/%s/%s%s",
        baseURLConfiguration.getWorkflowBaseUrl(), realmId, userId, USER_STORE_ADVANCE_SEARCH_URL);
  }

  public String getUserNameAvailabilityCheckUrl() {
    return String.format("%s%s", baseURLConfiguration.getMatrixBaseUrl(), USER_NAME_AVAILABILITY_URL);
  }

  public String getMatrixLoginApi() {
    return String.format("%s%s", baseURLConfiguration.getMatrixBaseUrl(), MATRIX_LOGIN_API);
  }

  public String findByIdGemsUrl(String realmId, String userId, String entityName, String id) {
    return String.format(
            "%s%s/%s%s/%s/%s/%s",
            baseURLConfiguration.getGemsBaseUrl(), GEMS_PREFIX_URL, realmId, GEMS_USER_PREFIX_URL, userId, entityName, id);
  }
  public String createCommunityUrl(String realmId, String userId, String entityName) {
    return String.format(
            "%s%s/%s%s/%s/%s",
            baseURLConfiguration.getGemsBaseUrl(), GEMS_PREFIX_URL, realmId, GEMS_USER_PREFIX_URL, userId, entityName);
  }

  public String createAdvancedSearchUrl(String realmId, String userId, String entityName) {
    return String.format(
        "%s%s/%s%s/%s/%s/%s",
        baseURLConfiguration.getGemsBaseUrl(), GEMS_PREFIX_URL, realmId, GEMS_USER_PREFIX_URL, userId, entityName,ADVANCED_SEARCH_PREFIX);
  }

  public String kickUserFromRoom(String roomId) {
    return String.format("%s%s/%s%s", baseURLConfiguration.getMatrixBaseUrl(), KICK_USER_FROM_ROOM_URL_PREFIX, roomId, KICK_USER_FROM_ROOM_URL_SUFFIX);
  }


  public String createSearchUrl( String entityName) {
    return String.format(
        "%s/%s/%s",
        baseURLConfiguration.getElasticSearchUrl(), entityName, ELASTIC_SEARCH_SUFFIX);
  }
  public String checkUserNameAvailability(String userName) {
   return baseURLConfiguration.getMatrixBaseUrl()+CHECK_USER+userName;
  }
  public String nfcChatUrl(String chatRoomId){
    return String.format("%s%s%s%s", baseURLConfiguration.getNfcBaseUrl(),NFC_CHAT_URL_PREFIX,chatRoomId,NFC_CHAT_URL_SUFFIX);
  }
  public String getOAuthUrl() {
    return String.format("%s%s", baseURLConfiguration.getOAuthBaseUrl(), OAUTH_URL);
  }
}
