package com.carbonbuddy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private JwtProperties jwt = new JwtProperties();
    private SeedProperties seed = new SeedProperties();

    public JwtProperties getJwt() {
        return jwt;
    }

    public void setJwt(JwtProperties jwt) {
        this.jwt = jwt;
    }

    public SeedProperties getSeed() {
        return seed;
    }

    public void setSeed(SeedProperties seed) {
        this.seed = seed;
    }
}
