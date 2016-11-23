import org.aeonbits.owner.Config;

public interface Configuration extends Config {
    @Key("aws.credentials.accessKeyId")
    @DefaultValue("provide-configuration-file")
    String getAccessKeyId();

    @Key("aws.credentials.secretAccessKey")
    @DefaultValue("provide-configuration-file")
    String getSecretAccessKey();
}
