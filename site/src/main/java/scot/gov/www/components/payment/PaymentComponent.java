package scot.gov.www.components.payment;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.resourcebundle.ResourceBundleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.Local;
import scot.gov.www.components.PolicyComponent;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by z441571 on 04/12/2019.
 */
public class PaymentComponent extends BaseHstComponent {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentComponent.class);

    @Override
    public void doBeforeRender(final HstRequest request,
                               final HstResponse response) {

        HstRequestContext context = request.getRequestContext();
        String path = context.getBaseURL().getRequestPath();
        String[] parts = path.split("/");
        String result = parts[parts.length-1];
        LOG.info("Default locale is {}, available locales are {}",
                Arrays.stream(Locale.getAvailableLocales())
                        .map(Locale::toString)
                        .collect(Collectors.joining(", ")));
        ResourceBundle bundle = ResourceBundleUtils.getBundle(
                ResourceBundleMessage.valueOf(result.toUpperCase()).getParamName(), Locale.getDefault());

        PaymentResult paymentResult = new PaymentResult(bundle.getString("title"), bundle.getString("content"));

        request.setAttribute("paymentResult", paymentResult);

    }
}
