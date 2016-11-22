import org.aeonbits.owner.Config;

public interface Configuration extends Config {
    @Key("aws.credentials.accessKeyId")
    String getAccessKeyId();

    @Key("aws.credentials.secretAccessKey")
    String getSecretAccessKey();
}
