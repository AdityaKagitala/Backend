package com.example.urlshortener.service;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * Simple helper that checks TXT records for a token substring.
 */
public class DomainDnsVerifier {

    /**
     * Return true if any TXT record value contains the token.
     * Example token: "urlmaster-verification=abc123..."
     */
    public static boolean txtRecordContains(String domain, String token) {
        try {
            Lookup lookup = new Lookup(domain, Type.TXT);
            Record[] records = lookup.run();
            if (records == null) return false;
            for (Record r : records) {
                String txt = r.rdataToString();
                if (txt == null) continue;
                // remove surrounding quotes added by some resolvers
                txt = txt.replace("\"", "");
                if (txt.contains(token)) return true;
            }
        } catch (TextParseException e) {
            // invalid domain name
            return false;
        } catch (Exception e) {
            // network / resolver error -> treat as not verified
            return false;
        }
        return false;
    }
}