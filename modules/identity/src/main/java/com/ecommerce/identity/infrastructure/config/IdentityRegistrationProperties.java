package com.ecommerce.identity.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity.registration")
public class IdentityRegistrationProperties {

    private int maxVerifyAttempts = 5;
    private int resendCooldownSeconds = 60;
    private int otpExpirySeconds = 300;
    private boolean exposeOtpInResponse;
    private Captcha captcha = new Captcha();
    private Twilio twilio = new Twilio();

    public int getMaxVerifyAttempts() {
        return maxVerifyAttempts;
    }

    public void setMaxVerifyAttempts(int maxVerifyAttempts) {
        this.maxVerifyAttempts = maxVerifyAttempts;
    }

    public int getResendCooldownSeconds() {
        return resendCooldownSeconds;
    }

    public void setResendCooldownSeconds(int resendCooldownSeconds) {
        this.resendCooldownSeconds = resendCooldownSeconds;
    }

    public int getOtpExpirySeconds() {
        return otpExpirySeconds;
    }

    public void setOtpExpirySeconds(int otpExpirySeconds) {
        this.otpExpirySeconds = otpExpirySeconds;
    }

    public boolean isExposeOtpInResponse() {
        return exposeOtpInResponse;
    }

    public void setExposeOtpInResponse(boolean exposeOtpInResponse) {
        this.exposeOtpInResponse = exposeOtpInResponse;
    }

    public Captcha getCaptcha() {
        return captcha;
    }

    public void setCaptcha(Captcha captcha) {
        this.captcha = captcha;
    }

    public Twilio getTwilio() {
        return twilio;
    }

    public void setTwilio(Twilio twilio) {
        this.twilio = twilio;
    }

    public static class Captcha {
        private String provider = "mock";
        private String expectedToken;
        private String googleSiteKey;
        private String googleSecretKey;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getExpectedToken() {
            return expectedToken;
        }

        public void setExpectedToken(String expectedToken) {
            this.expectedToken = expectedToken;
        }

        public String getGoogleSiteKey() {
            return googleSiteKey;
        }

        public void setGoogleSiteKey(String googleSiteKey) {
            this.googleSiteKey = googleSiteKey;
        }

        public String getGoogleSecretKey() {
            return googleSecretKey;
        }

        public void setGoogleSecretKey(String googleSecretKey) {
            this.googleSecretKey = googleSecretKey;
        }
    }

    public static class Twilio {
        private boolean enabled;
        private String accountSid;
        private String authToken;
        private String fromPhoneNumber;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAccountSid() {
            return accountSid;
        }

        public void setAccountSid(String accountSid) {
            this.accountSid = accountSid;
        }

        public String getAuthToken() {
            return authToken;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        public String getFromPhoneNumber() {
            return fromPhoneNumber;
        }

        public void setFromPhoneNumber(String fromPhoneNumber) {
            this.fromPhoneNumber = fromPhoneNumber;
        }
    }
}
