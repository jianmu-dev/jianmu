package dev.jianmu.embedded.worker.aggregate.spec;

import java.util.List;
import java.util.Map;

/**
 * @author Ethan Liu
 * @class PodSpec
 * @description PodSpec
 * @create 2022-01-09 12:26
 */
public class PodSpec {
    private String name;
    private String namespace;
    private Map<String, String> annotations;
    private Map<String, String> labels;
    private String nodeName;
    private Map<String, String> nodeSelector;
    private List<Toleration> tolerations;
    private String serviceAccountName;
    private List<HostAlias> hostAliases;
    private DnsConfig dnsConfig;

    public static class Toleration {
        private String effect;
        private String key;
        private String operator;
        private int tolerationSeconds;
        private String value;
    }

    public static class HostAlias {
        private String ip;
        private List<String> hostnames;
    }

    public static class DnsConfig {
        private List<String> nameservers;
        private List<String> searches;
        private List<DNSConfigOptions> options;

        public static class DNSConfigOptions {
            private String name;
            private String value;
        }
    }
}
