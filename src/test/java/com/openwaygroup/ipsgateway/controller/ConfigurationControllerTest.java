package com.openwaygroup.ipsgateway.controller;

import com.openwaygroup.ipsgateway.IpsGatewayApplication;
import com.openwaygroup.ipsgateway.entities.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@ContextConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest
class ConfigurationControllerTest {
    private MockMvc mockMvc;
    public Socket socket;
    @Autowired
    private Configuration configuration;
    org.slf4j.Logger log = LoggerFactory.getLogger(IpsGatewayApplication.class);
    ConfigurationController configurationController = new ConfigurationController();
   /* ConfigurationProperties configurationProperties = new ConfigurationProperties();*/

    void setConfigurationMockHost(){
        this.configuration.setHostIp("4.2.2.2");
        this.configuration.setHostPort(53);
        this.configuration.setRole(false);
    }
    void setConfigurationMockVts() throws UnknownHostException {
        this.configuration.setIpsIp(configurationController.getLocalHostAddress());
        this.configuration.setIpsPort(9999);
        this.configuration.setRole(true);
    }
   void clientDemo() throws UnknownHostException,
            IOException {
        String sentence_to_server;
        String sentence_from_server;
        socket = new Socket(this.configuration.getIpsIp(), this.configuration.getIpsPort());
      /*  sentence_to_server = "Hello World!";
        DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outToServer.writeBytes(sentence_to_server + '\n');
        sentence_from_server = inFromServer.readLine();
        System.out.println("FROM SERVER: " + sentence_from_server);*/
    }

    @Test
    void testConnectionInitializeClient() throws IOException {
        boolean expectedResult = true;
        setConfigurationMockHost();
        configurationController.update(this.configuration);
        boolean actualResult = this.configuration.getStatus();
        assertEquals(expectedResult,actualResult);
    }
    @Test
    void testConnectionInitializeServer() throws IOException{
        boolean expectedResult = true;
        setConfigurationMockVts();
        boolean actualResult = configurationController.initializeServer(this.configuration);
        assertEquals(expectedResult,actualResult);
    }
    @Test
    void testConfigurationIndex() throws Exception {
        this.mockMvc
                .perform(get("/configuration"))
                .andExpect(status().isOk())
                .andExpect(view().name("configuration"))
                .andExpect(model().attributeExists("configuration"));
    }
    @Test
    void testLostConnectionToServer() throws InterruptedException {
        boolean expectedResult  = false;
        setConfigurationMockHost();
        configurationController.initializeClient(this.configuration);
        Thread.sleep(10000);
        configuration.setHostIp("55.12.34.53");
        Thread.sleep(10000);
        boolean actualResult = this.configuration.getStatus();
        assertEquals(expectedResult, actualResult);
    }
    @Test
    void testReconnectToServer() throws InterruptedException {
        boolean expectedResult  = true;
        setConfigurationMockHost();
        configurationController.initializeClient(this.configuration);
        Thread.sleep(10000);
        configuration.setHostIp("55.12.34.53");
        Thread.sleep(10000);
        configuration.setHostIp("4.2.2.2");
        Thread.sleep(10000);
        boolean actualResult = this.configuration.getStatus();
        assertEquals(expectedResult, actualResult);
    }
    @Test
    void testChangeLogLevel() {
        configurationController.setLogLevel("info");
        log.info("[getMessage] info message");
        log.warn("[getMessage] warn message");
        log.error("[getMessage] error message");
        log.debug("[getMessage] debug message");
        log.trace("[getMessage] trace message");
    }
    @Test
    void testVtsServerHasMultipleConnection() throws IOException {
        Integer expectedResult  = 3;
        setConfigurationMockVts();
        configurationController.initializeServer(this.configuration);
        clientDemo();
        clientDemo();
        clientDemo();
        Integer actualResult = this.configuration.getClientNumber();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testClientDisconnectFromVtsServer() throws IOException {
        Integer expectedResult  = 2;
        setConfigurationMockVts();
        configurationController.initializeServer(this.configuration);
        clientDemo();
        clientDemo();
        clientDemo();
        socket.close();
        Integer actualResult = this.configuration.getClientNumber();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testShutDownServer() throws Exception {
        boolean expectedResult = false;
        setConfigurationMockVts();
        configurationController.initializeServer(this.configuration);
        configurationController.closeConnection();
        boolean actualResult = this.configuration.getStatus();
        assertEquals(expectedResult,actualResult);
    }
    @Test
    void testCloseConnectionToServer() throws Exception {
        boolean expectedResult = false;
        setConfigurationMockHost();
        configurationController.initializeClient(this.configuration);
        configurationController.closeConnection();
        boolean actualResult = this.configuration.getStatus();
        assertEquals(expectedResult,actualResult);
    }

    @Test
    void testImportConfigurationFile(){
        System.out.println(this.configuration.getHostIp());
        System.out.println(this.configuration.getHostPort());
        System.out.println(this.configuration.getRole());
    }
}
