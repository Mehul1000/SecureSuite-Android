package com.nuvolect.securesuite.webserver;//

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.nuvolect.securesuite.main.App;
import com.nuvolect.securesuite.main.CConst;
import com.nuvolect.securesuite.util.Cryp;
import com.nuvolect.securesuite.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/** Utilitiy methods to support web app classes */
public class WebUtil {

    private static String m_ip_address;

    /**
     * Test for wifi.
     * @param ctx
     * @return
     */
    public static boolean wifiEnabled(Context ctx) {

        ConnectivityManager cm =
                (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Confirm that the device trying to connect is approved.
     * Approval can temporarily be disabled during the pairing process.
     */
    public static class NullHostNameVerifier implements HostnameVerifier {

        /**
         * Only allow registration when device is in pairing mode, this is the
         * only time host verification is disabled.
         */
        public boolean m_hostVerifierEnabled = true;
        private static NullHostNameVerifier instance;

        public static synchronized NullHostNameVerifier getInstance() {
            if(instance == null) {
                instance = new NullHostNameVerifier();
            }
            return instance;
        }

        @Override
        public boolean verify(String hostname, SSLSession session) {

            if( ! m_hostVerifierEnabled){

                LogUtil.log(LogUtil.LogType.WEB_SERVER, "Host verifier disabled, Certificate approved for " + hostname);
                return true;
            }else
            if( hostname.contentEquals( getCompanionServerIp())
             || hostname.contentEquals( m_ip_address )){

                LogUtil.log(LogUtil.LogType.WEB_SERVER, "Certificate approved for " + hostname);
                return true;
            }
            else{

                LogUtil.log(LogUtil.LogType.WEB_SERVER, "Certificate denied for " + hostname);
                return false;
            }
        }

        public void setHostVerifierEnabled(boolean hostVerifierEnabled) {

            LogUtil.log(LogUtil.LogType.WEB_SERVER, "setHostVerifierEnabled(): " + hostVerifierEnabled);

            this.m_hostVerifierEnabled = hostVerifierEnabled;
        }
    }
    /**
     *
     Default WiFi address:
     http://stackoverflow.com/questions/17302220/android-get-ip-address-of-a-hotspot-providing-device

     How to see if Android is connected to WiFi:
     http://stackoverflow.com/questions/3841317/how-to-see-if-wifi-is-connected-in-android?rq=1

     How to check if the WiFi hotspot is enabled:
     http://stackoverflow.com/questions/12401108/how-to-check-programmatically-if-hotspot-is-enabled-or-disabled

     Android Find the device's ip address when it's hosting a hotspot
     http://stackoverflow.com/questions/21804891/android-find-the-devices-ip-address-when-its-hosting-a-hotspot

     */

    /**
     * Get the formatted IP address of Android
     * @param ctx
     * @return
     */
    public static String getServerIp(Context ctx) {

        WifiManager wifiManager = (WifiManager) ctx.getSystemService(ctx.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        /**
         * Look for 0.0.0.0 and test to see if the WiFi hotspot is active.
         * If so substitute the default WiFi IP address.
         *
         * Android default IP address:
         * http://stackoverflow.com/questions/17302220/android-get-ip-address-of-a-hotspot-providing-device
         */
        if( ipAddress == 0 && isWiFiHotSpotActive(wifiManager))
            m_ip_address = "192.168.43.1";
        else
            m_ip_address = String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff),
                    (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff),
                    (ipAddress >> 24 & 0xff));

        return m_ip_address;
    }

    /**
     * Check if the Android device is running a hotspot.
     * @param wifiManager
     * @return
     */
    public static boolean isWiFiHotSpotActive(WifiManager wifiManager)
    {
        try {

            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true); //in the case of visibility change in future APIs
            return (Boolean) method.invoke(wifiManager);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getServerIpPort(Context ctx){

        return getServerIp(ctx)+":"+ getPort(ctx );
    }
    /**
     * Return the URL of the app on the local area network
     * @param ctx
     * @return
     */
    public static String getServerUrl(Context ctx){

        return "https://"+ getServerIpPort(ctx);
    }

    /**
     * Return URL of the app on the LAN append with a page
     * @param ctx
     * @param page
     * @return
     */
    public static String getServerUrl(Context ctx, String page) {

        return getServerUrl(ctx)+"/"+ page;
    }

    /**
     * Return the full ip and port number in 0.0.0.0:0000 format.
     * @return
     */
    public static String getCompanionServerIpPort() {

        return Cryp.get(CConst.COMPANION_IP_PORT, App.DEFAULT_IP_PORT);
    }

    /**
     * Return just the IP in the form 0.0.0.0
     * @return
     */
    public static String getCompanionServerIp() {

        String ipAndPort = Cryp.get(CConst.COMPANION_IP_PORT, App.DEFAULT_IP_PORT);

        String[] parts = ipAndPort.split("\\:");

        return parts[0];
    }

    public static void setCompanionServerIpPort(String companion_ip_port) {

        Cryp.put(CConst.COMPANION_IP_PORT, companion_ip_port);
    }

    public static boolean companionServerAssigned() {
        return ! getCompanionServerIpPort().contains(App.DEFAULT_IP_PORT);
    }

    public static String getCompanionServerUrl(){

        return "https://"+ Cryp.get( CConst.COMPANION_IP_PORT, App.DEFAULT_IP_PORT);
    }

    public static String getCompanionServerUrl(String page) {

        return getCompanionServerUrl()+"/"+ page;
    }

    public static String buildUrl(String endPoint, Map<String, String> params) {

        String result = "";
        String urlStr = endPoint+"?";

        int i = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if( i++!=0)
                urlStr += "&";

            urlStr += entry.getKey();
            urlStr += "=";
            urlStr += entry.getValue();
        }

        try {
            URL url = new URL(urlStr);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            url = uri.toURL();
            result = url.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Build and return a response code JSONObject.
     * @param response_code
     * @return
     */
    public static JSONObject response(int response_code) {

        JSONObject response = new JSONObject();
        try {
            response.put(CConst.RESPONSE_CODE, response_code);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Test for a specific response code
     * @param response
     * @return
     */
    public static boolean responseMatch(JSONObject response, int matchCode) {

        try {
            return response.getInt(CConst.RESPONSE_CODE) == matchCode;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Test for a specific response code
     * @param stringResponse
     * @return
     */
    public static boolean responseMatch(String stringResponse, int matchCode) {

        try {
            if( stringResponse.isEmpty())
                return false;

            JSONObject response = new JSONObject( stringResponse );
            return response.getInt(CConst.RESPONSE_CODE) == matchCode;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Return the current port number and if undefined, use default port number.
     * @return int port number
     */
    public static int getPort(Context ctx){

        return Cryp.getInt(ctx, CConst.PORT, App.DEFAULT_PORT);
    }

    public static void setPort(Context ctx, int port){

        Cryp.putInt( ctx, CConst.PORT, port);
    }

    /**
     * Generate a random 4 digit port number.
     * @return int port number.
     */
    public static int getRandomPort(){

        int port = (int)(Math.random() * (9999 - 1025) + 1025);

        return port;
    }
}
