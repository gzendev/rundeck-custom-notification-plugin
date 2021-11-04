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

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.descriptions.SelectValues;
import com.dtolabs.rundeck.plugins.descriptions.TextArea;
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;

@Plugin(service="Notification",name="CustomWebhookNotificationPlugin")
@PluginDescription(title="Custom Webhook Notification", description="A notification plugin that makes customized HTTP requests")
public class CustomWebhookNotificationPlugin implements NotificationPlugin {
  
  @PluginProperty(name = "webhookUrl", title = "Webhook URL", description = "The webhook url", required = false)
  private String webhookUrl;
  
  @PluginProperty(name = "contentType", title = "Content Type", description = "The content type header", required = false)
  private String contentType;
  
  @PluginProperty(name = "requestMethod", title = "Request Method", description = "The request method", required = false)
  @SelectValues(values = {"GET", "POST", "PUT", "DELETE"})
  private String requestMethod;
  
  @PluginProperty(name = "messageBody", title = "Message Body", description = "The message body", required = false)
  @TextArea
  private String messageBody;
  
  
  private String sendMessage() throws IOException, CustomMessageException {
    
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      
      ResponseHandler<String> responseHandler = response -> {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : null;
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
      };
      
      switch (requestMethod) {
        case "GET":
          HttpGet httpget = new HttpGet(webhookUrl);
          httpget.setHeader("Accept", contentType);
          httpget.setHeader("Content-type", contentType);
          return httpclient.execute(httpget, responseHandler);
        case "POST":
          HttpPost httpPost = new HttpPost(webhookUrl);
          httpPost.setHeader("Accept", contentType);
          httpPost.setHeader("Content-type", contentType);
          httpPost.setEntity(new StringEntity(messageBody));
          return httpclient.execute(httpPost, responseHandler);
        case "PUT":
          HttpPut httpPut = new HttpPut(webhookUrl);
          httpPut.setHeader("Accept", contentType);
          httpPut.setHeader("Content-type", contentType);
          httpPut.setEntity(new StringEntity(messageBody));
          return httpclient.execute(httpPut, responseHandler);
        case "DELETE":
          HttpDelete httpDelete = new HttpDelete(webhookUrl);
          httpDelete.setHeader("Accept", contentType);
          httpDelete.setHeader("Content-type", contentType);
          return httpclient.execute(httpDelete, responseHandler);
        default:
          throw new CustomMessageException("Undefined request method");
      }
    }
  }

  @Override
  public boolean postNotification(String trigger, Map executionData, Map config) {
    try {
      sendMessage();
    } catch (IOException | CustomMessageException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

}
