import org.aeonbits.owner.Config;

public interface Configuration extends Config {

    String DEFAULT_VALUE = "provide-configuration-file";

    @Key("aws.credentials.accessKeyId")
    @DefaultValue(DEFAULT_VALUE)
    String getAccessKeyId();

    @Key("aws.credentials.secretAccessKey")
    @DefaultValue(DEFAULT_VALUE)
    String getSecretAccessKey();

    @Key("aws.region")
    @DefaultValue(DEFAULT_VALUE)
    String getRegion();

    @Key("aws.imageId")
    @DefaultValue(DEFAULT_VALUE)
    String getImageId();
}
