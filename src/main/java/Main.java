import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.aeonbits.owner.ConfigFactory;
import org.jclouds.ContextBuilder;
import org.jclouds.aws.ec2.reference.AWSEC2Constants;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.ec2.domain.InstanceType;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final Configuration CONFIG = ConfigFactory.create(Configuration.class);

    private static final String IMAGE_ID = "eu-west-1/ami-0c590f7f";
    private static final int MAX_RETRY_TIMES = 10;

    private static int retriedTimes;

    public static void main(String[] args) {
        Properties overrides = new Properties();
        overrides.setProperty(AWSEC2Constants.PROPERTY_EC2_AMI_QUERY, "");
        overrides.setProperty(AWSEC2Constants.PROPERTY_EC2_CC_AMI_QUERY, "");
        overrides.setProperty(AWSEC2Constants.PROPERTY_EC2_CC_REGIONS, "eu-west-1");

        ComputeServiceContext context = ContextBuilder.newBuilder("aws-ec2")
                .overrides(overrides)
                .credentials(CONFIG.getAccessKeyId(), CONFIG.getSecretAccessKey())
                .modules(ImmutableSet.<Module>of(new SLF4JLoggingModule(), new SshjSshClientModule()))
                .buildView(ComputeServiceContext.class);
        LOGGER.info("Created compute service context");

        NodeMetadata nodeMetadata = createNode(context);

        try (PrintWriter out = new PrintWriter("index.html")) {
            LOGGER.info("Downloading the page");
            String body = getWebPageBody(nodeMetadata != null ? nodeMetadata.getPublicAddresses().iterator().next() : "");
            LOGGER.info("Body: {}", body);

            out.print(body);
            LOGGER.info("Body saved to file");
        } catch (Exception e) {
            LOGGER.error("Page downloading error", e);
        } finally {
            terminateNode(context, nodeMetadata);
            LOGGER.info("Closing context");
            context.close();
        }
    }

    private static NodeMetadata createNode(ComputeServiceContext context) {
        Template template = context.getComputeService().templateBuilder()
                .imageId(IMAGE_ID)
                .hardwareId(InstanceType.T2_MICRO)
                .build();

        LOGGER.info("Found the image: {}", template.getImage().getName());

        try {
            Stopwatch timer = Stopwatch.createStarted();

            NodeMetadata nodeMetadata = context.getComputeService().createNodesInGroup("default", 1, template).iterator().next();
            LOGGER.info("Created instance: {}", nodeMetadata.getName());

            timer.stop();
            LOGGER.info("Instance startup took: {} ms", timer.elapsed(TimeUnit.MILLISECONDS));

            return nodeMetadata;
        } catch (Exception e) {
            LOGGER.error("Node running error", e);
            context.close();
            return null;
        }
    }

    private static void terminateNode(ComputeServiceContext context, NodeMetadata nodeMetadata) {
        LOGGER.info("Terminating instance: {}", nodeMetadata.getName());
        context.getComputeService().destroyNode(nodeMetadata.getId());
    }

    private static String getWebPageBody(String ipAddress) throws ConnectException {
        try {
            return Jsoup.connect("http://" + ipAddress).get().html();
        } catch (IOException e) {
            if (++retriedTimes < MAX_RETRY_TIMES) {
                sleep(5000);
                LOGGER.debug("Retrying http connection...");
                return getWebPageBody(ipAddress);
            }
            throw new ConnectException("Cannot download the page");
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.error("Sleep interrupted");
        }
    }
}
