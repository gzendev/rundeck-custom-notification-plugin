package com.rundeck.custom.notification.plugin;

import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class CustomWebhookNotificationPluginTest {

  private CustomWebhookNotificationPlugin plugin;
  private Map<String, Object> config = new HashMap<String, Object>();
  private Map<String, Object> executionData = new HashMap<String, Object>();
  private String trigger;

  private String webhookUrl;
  private String contentType;
  private String requestMethod;
  private String messageBody;

  private boolean ret;

  private static final String METHOD_GET = "GET";
  private static final String METHOD_POST = "POST";
  private static final String METHOD_PUT = "PUT";
  private static final String METHOD_DELETE = "DELETE";

  private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
  private static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";
  private static final String CONTENT_TYPE_TEXT_HTML = "text/html";
  private static final String CONTENT_TYPE_APPLICATION_URLENCODED = "application/x-www-form-urlencoded";


  private void settingAttrs(String webhookUrl, String requestMethod, String contentType, String messageBody) {
    this.webhookUrl = webhookUrl;
    this.requestMethod = requestMethod;
    this.contentType = contentType;
    this.messageBody = messageBody;
    this.trigger = "success";
  }

  @Test
  public void canHandleEmptyUrl() throws CustomMessageException {
    settingAttrs("", METHOD_GET, CONTENT_TYPE_APPLICATION_JSON, "");
    this.plugin = new CustomWebhookNotificationPlugin(webhookUrl, requestMethod, contentType, messageBody);
    this.ret = this.plugin.postNotification(trigger, executionData, config);
    assertFalse(ret);
  }

  @Test
  public void canHandleBadUrl() throws CustomMessageException {
    settingAttrs("www.test.com", METHOD_GET, CONTENT_TYPE_APPLICATION_JSON, "");
    this.plugin = new CustomWebhookNotificationPlugin(webhookUrl, requestMethod, contentType, messageBody);
    this.ret = this.plugin.postNotification(trigger, executionData, config);
    assertFalse(ret);
  }

  @Test
  public void callExistingUrl() {
    settingAttrs("https://www.google.com", METHOD_GET, CONTENT_TYPE_APPLICATION_JSON, "");
    this.plugin = new CustomWebhookNotificationPlugin(webhookUrl, requestMethod, contentType, messageBody);
    this.ret = this.plugin.postNotification(trigger, executionData, config);
    assertTrue(ret);
  }

  @Test
  public void callNotExistingUrl() {
    settingAttrs("http://test.com", METHOD_GET, CONTENT_TYPE_APPLICATION_JSON, "");
    this.plugin = new CustomWebhookNotificationPlugin(webhookUrl, requestMethod, contentType, messageBody);
    this.ret = this.plugin.postNotification(trigger, executionData, config);
    assertFalse(ret);
  }

  @Test
  public void canHandleEmptyContentType() {
    settingAttrs("https://www.google.com", METHOD_POST, "", "Body Messages");
    this.plugin = new CustomWebhookNotificationPlugin(webhookUrl, requestMethod, contentType, messageBody);
    this.ret = this.plugin.postNotification(trigger, executionData, config);
    assertFalse(ret);
  }

  @Test
  public void canHandleNotExistingContentType() {
    settingAttrs("https://www.google.com", METHOD_POST, "123", "Body Messages");
    this.plugin = new CustomWebhookNotificationPlugin(webhookUrl, requestMethod, contentType, messageBody);
    this.ret = this.plugin.postNotification(trigger, executionData, config);
    assertFalse(ret);
  }

  @Test
  public void canHandleEmptyRequestMethod() {
    settingAttrs("https://www.google.com", "", CONTENT_TYPE_APPLICATION_JSON, "");
    this.plugin = new CustomWebhookNotificationPlugin(webhookUrl, requestMethod, contentType, messageBody);
    this.ret = this.plugin.postNotification(trigger, executionData, config);
    assertFalse(ret);
  }

  @Test
  public void canHandleNotExistingRequestMethod() {
    settingAttrs("https://www.google.com", "123", CONTENT_TYPE_APPLICATION_JSON, "");
    this.plugin = new CustomWebhookNotificationPlugin(webhookUrl, requestMethod, contentType, messageBody);
    this.ret = this.plugin.postNotification(trigger, executionData, config);
    assertFalse(ret);
  }

  @Test
  public void canHandleEmptyMessageBody() {
    settingAttrs("https://www.google.com", METHOD_PUT, CONTENT_TYPE_APPLICATION_JSON, "");
    this.plugin = new CustomWebhookNotificationPlugin(webhookUrl, requestMethod, contentType, messageBody);
    this.ret = this.plugin.postNotification(trigger, executionData, config);
    assertFalse(ret);
  }

  @Test
  public void allRequiredFieldsCompleted () {
    settingAttrs("https://jsonplaceholder.typicode.com/posts", METHOD_POST, CONTENT_TYPE_APPLICATION_JSON, "{\"rundeck\": \"success\"}");
    this.plugin = new CustomWebhookNotificationPlugin(webhookUrl, requestMethod, contentType, messageBody);
    this.ret = this.plugin.postNotification(trigger, executionData, config);
    assertTrue(ret);
  }

}
