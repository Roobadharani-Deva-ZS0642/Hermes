package org.intics.hermes.common;

public class ConfigurationVariable {

    private ConfigurationVariable(){

    }

    public static final String LOGIN_URL = "alchemy-server/alchemy/api/v2/auth/login";

    public static final String INBOUND_URL = "alchemy-server/alchemy/api/v2/transaction/create/pipeline/fileUrl";

    public static final String SHADOW_INBOUND_URL = "alchemy-server/alchemy/api/v2/transaction/create/pipeline/shadow/fileUrl";

}
