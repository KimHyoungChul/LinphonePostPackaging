package com.example.linphone;

import org.linphone.core.AVPFMode;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Core;
import org.linphone.core.CoreException;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;
import org.linphone.mediastream.Log;

public class AccountBuilder {
        private Core lc;
        private String tempUsername;
        private String tempDisplayName;
        private String tempUserId;
        private String tempPassword;
        private String tempHa1;
        private String tempDomain;
        private String tempProxy;
        private String tempRealm;
        private String tempPrefix;
        private boolean tempOutboundProxy;
        private String tempContactsParams;
        private String tempExpire;
        private TransportType tempTransport;
        private boolean tempAvpfEnabled = false;
        private int tempAvpfRRInterval = 0;
        private String tempQualityReportingCollector;
        private boolean tempQualityReportingEnabled = false;
        private int tempQualityReportingInterval = 0;
        private boolean tempEnabled = true;
        private boolean tempNoDefault = false;


        public AccountBuilder(Core lc) {
            this.lc = lc;
        }

        public AccountBuilder setTransport(TransportType transport) {
            tempTransport = transport;
            return this;
        }

        public AccountBuilder setUsername(String username) {
            tempUsername = username;
            return this;
        }

        public AccountBuilder setDisplayName(String displayName) {
            tempDisplayName = displayName;
            return this;
        }

        public AccountBuilder setPassword(String password) {
            tempPassword = password;
            return this;
        }

        public AccountBuilder setHa1(String ha1) {
            tempHa1 = ha1;
            return this;
        }

        public AccountBuilder setDomain(String domain) {
            tempDomain = domain;
            return this;
        }

        public AccountBuilder setServerAddr(String proxy) {
            tempProxy = proxy;
            return this;
        }

        public AccountBuilder setOutboundProxyEnabled(boolean enabled) {
            tempOutboundProxy = enabled;
            return this;
        }

        public AccountBuilder setContactParameters(String contactParams) {
            tempContactsParams = contactParams;
            return this;
        }

        public AccountBuilder setExpires(String expire) {
            tempExpire = expire;
            return this;
        }

        public AccountBuilder setUserid(String userId) {
            tempUserId = userId;
            return this;
        }

        public AccountBuilder setAvpfEnabled(boolean enable) {
            tempAvpfEnabled = enable;
            return this;
        }

        public AccountBuilder setAvpfRrInterval(int interval) {
            tempAvpfRRInterval = interval;
            return this;
        }

        public AccountBuilder setRealm(String realm) {
            tempRealm = realm;
            return this;
        }

        public AccountBuilder setQualityReportingCollector(String collector) {
            tempQualityReportingCollector = collector;
            return this;
        }

        public AccountBuilder setPrefix(String prefix) {
            tempPrefix = prefix;
            return this;
        }

        public AccountBuilder setQualityReportingEnabled(boolean enable) {
            tempQualityReportingEnabled = enable;
            return this;
        }

        public AccountBuilder setQualityReportingInterval(int interval) {
            tempQualityReportingInterval = interval;
            return this;
        }

        public AccountBuilder setEnabled(boolean enable) {
            tempEnabled = enable;
            return this;
        }

        public AccountBuilder setNoDefault(boolean yesno) {
            tempNoDefault = yesno;
            return this;
        }

        /**
         * Creates a new account
         *
         * @throws CoreException
         */
        public void saveNewAccount() throws CoreException {

            if (tempUsername == null || tempUsername.length() < 1 || tempDomain == null || tempDomain.length() < 1) {
                Log.w("Skipping account save: username or domain not provided");
                return;
            }

            String identity = "sip:" + tempUsername + "@" + tempDomain;
            String proxy = "sip:";
            if (tempProxy == null) {
                proxy += tempDomain;
            } else {
                if (!tempProxy.startsWith("sip:") && !tempProxy.startsWith("<sip:")
                        && !tempProxy.startsWith("sips:") && !tempProxy.startsWith("<sips:")) {
                    proxy += tempProxy;
                } else {
                    proxy = tempProxy;
                }
            }

            Address proxyAddr = Factory.instance().createAddress(proxy);
            Address identityAddr = Factory.instance().createAddress(identity);
            if (proxyAddr == null || identityAddr == null) {
                throw new CoreException("Proxy or Identity address is null !");
            }

            if (tempDisplayName != null) {
                identityAddr.setDisplayName(tempDisplayName);
            }

            if (tempTransport != null) {
                proxyAddr.setTransport(tempTransport);
            }

            String route = tempOutboundProxy ? proxyAddr.asStringUriOnly() : null;

            ProxyConfig prxCfg = lc.createProxyConfig();
            prxCfg.setIdentityAddress(identityAddr);
            prxCfg.setServerAddr(proxyAddr.asStringUriOnly());
            prxCfg.setRoute(route);
            prxCfg.enableRegister(tempEnabled);

            if (tempContactsParams != null)
                prxCfg.setContactUriParameters(tempContactsParams);
            if (tempExpire != null) {
                prxCfg.setExpires(Integer.parseInt(tempExpire));
            }

            prxCfg.setAvpfMode(AVPFMode.Disabled);
            prxCfg.setAvpfRrInterval(tempAvpfRRInterval);
            prxCfg.enableQualityReporting(tempQualityReportingEnabled);
            prxCfg.setQualityReportingCollector(tempQualityReportingCollector);
            prxCfg.setQualityReportingInterval(tempQualityReportingInterval);


            if (tempPrefix != null) {
                prxCfg.setDialPrefix(tempPrefix);
            }


            if (tempRealm != null)
                prxCfg.setRealm(tempRealm);

            AuthInfo authInfo = Factory.instance().createAuthInfo(tempUsername, tempUserId, tempPassword, tempHa1, tempRealm, tempDomain);

            lc.addProxyConfig(prxCfg);
            lc.addAuthInfo(authInfo);

            if (!tempNoDefault)
                lc.setDefaultProxyConfig(prxCfg);
        }



}
