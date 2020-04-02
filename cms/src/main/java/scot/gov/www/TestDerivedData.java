package scot.gov.www;

import org.hippoecm.repository.ext.DerivedDataFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Value;

import java.util.Map;

public class TestDerivedData extends DerivedDataFunction {
    private static final Logger LOG = LoggerFactory.getLogger(FoiNumberDerivedData.class);

    public Map<String,Value[]> compute(Map<String,Value[]> parameters) {

        return parameters;
    }

}
