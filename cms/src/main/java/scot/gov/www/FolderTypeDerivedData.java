package scot.gov.www;

import org.hippoecm.repository.ext.DerivedDataFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import java.util.Map;

public class FolderTypeDerivedData extends DerivedDataFunction {
    static final long serialVersionUID = 1;
    private static final Logger LOG = LoggerFactory.getLogger(FolderTypeDerivedData.class);

    static final String FOLDER_TYPE = "folderType";

    public Map<String,Value[]> compute(Map<String,Value[]> parameters) {
        if (parameters.isEmpty()|| parameters.get(FOLDER_TYPE).length == 0){
            return parameters;
        }

        try {

            // Get value in parameters
            Value publicationTypeValue = parameters.get(FOLDER_TYPE)[0];
            String existingFolderType = publicationTypeValue.getString();
            LOG.info("Folder type: {}, ", existingFolderType);

            // If it's 'new-publication-folder', leave it alone
            // What about complex doc?

            // If publicationType is minutes/speech or statement/foi override the folder type
            String newFolderType = "";
            if ("minutes".equals(existingFolderType)) {
                newFolderType = "new-minutes-folder";
            }

            if ("speech-statement".equals(existingFolderType)) {
                newFolderType = "new-speech-or-statement-folder";
            }

            if ("foi-eir-release".equals(existingFolderType)) {
                newFolderType = "new-foi-folder";
            }

            // Don't set if it's still 'new-publication-folder'
            if (!newFolderType.isEmpty()) {
                // This needs to set it as an array of strings - how?
                parameters.put(FOLDER_TYPE, new Value[] {getValueFactory().createValue(newFolderType)});
            }

        } catch (RepositoryException e) {
            LOG.error("Couldn't set folder type via derived data, {}", e);
        }

        return parameters;
    }

}
