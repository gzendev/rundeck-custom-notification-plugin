package com.rundeck.custom.notification.plugin;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.descriptions.SelectValues;
import com.dtolabs.rundeck.plugins.descriptions.TextArea;
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;

@Plugin(service="Notification", name="CustomWebhookNotificationPlugin")
@PluginDescription(title="Custom Webhook Notification", description="A notification plugin that makes customized HTTP requests")
public class CustomWebhookNotificationPlugin implements NotificationPlugin {

  private static final Logger log = LoggerFactory.getLogger(CustomWebhookNotificationPlugin.class);

  @PluginProperty(name = "webhookUrl", title = "Webhook URL", description = "The webhook url", required = true)
  private String webhookUrl;

  @PluginProperty(name = "contentType", title = "Content Type", description = "The content type header", required = true)
  private String contentType;

  @PluginProperty(name = "requestMethod", title = "Request Method", description = "The request method", required = true)
  @SelectValues(values = {HttpGet.METHOD_NAME,
          HttpPost.METHOD_NAME,
          HttpPut.METHOD_NAME,
          HttpDelete.METHOD_NAME})
  private String requestMethod;

  @PluginProperty(name = "messageBody", title = "Message Body", description = "The message body", required = true)
  @TextArea
  private String messageBody;

  public CustomWebhookNotificationPlugin() {}

  public CustomWebhookNotificationPlugin(String webhookUrl, String requestMethod,
                                         String contentType, String messageBody) {
    super();
    this.webhookUrl = webhookUrl;
    this.contentType = contentType;
    this.requestMethod = requestMethod;
    this.messageBody = messageBody;
  }

  private String sendMessage() throws IOException, CustomMessageException {

    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

      ResponseHandler<String> responseHandler = response -> {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
          HttpEntity entity = response.getEntity();
          return entity != null ? EntityUtils.toString(entity) : null;
        } else {
          log.info("Unexpected response status: " + status);
          throw new ClientProtocolException("Unexpected response status: " + status);
        }
      };

      switch (requestMethod) {
        case HttpGet.METHOD_NAME:
          HttpGet httpget = new HttpGet(webhookUrl);
          httpget.setHeader("Accept", contentType);
          httpget.setHeader("Content-type", contentType);
          return httpclient.execute(httpget, responseHandler);
        case HttpPost.METHOD_NAME:
          HttpPost httpPost = new HttpPost(webhookUrl);
          httpPost.setHeader("Accept", contentType);
          httpPost.setHeader("Content-type", contentType);
          httpPost.setEntity(new StringEntity(messageBody));
          return httpclient.execute(httpPost, responseHandler);
        case HttpPut.METHOD_NAME:
          HttpPut httpPut = new HttpPut(webhookUrl);
          httpPut.setHeader("Accept", contentType);
          httpPut.setHeader("Content-type", contentType);
          httpPut.setEntity(new StringEntity(messageBody));
          return httpclient.execute(httpPut, responseHandler);
        case HttpDelete.METHOD_NAME:
          HttpDelete httpDelete = new HttpDelete(webhookUrl);
          httpDelete.setHeader("Accept", contentType);
          httpDelete.setHeader("Content-type", contentType);
          return httpclient.execute(httpDelete, responseHandler);
        default:
          log.info("Undefined Request Method");
          throw new CustomMessageException("Undefined Request Method");
      }
    } catch (Exception e) {
      log.info(e.getMessage());
      throw new IOException();
    }
  }

  @Override
  public boolean postNotification(String trigger, Map executionData, Map config) {
    try {
      sendMessage();
    } catch (IOException | CustomMessageException e) {
      log.info(e.getMessage());
      return false;
    }
    return true;
  }

}