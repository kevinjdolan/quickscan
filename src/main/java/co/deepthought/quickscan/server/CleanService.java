package co.deepthought.quickscan.server;

import co.deepthought.quickscan.store.DocumentStore;
import com.sleepycat.je.DatabaseException;

/**
 * A service deleting documents from the store.
 */
public class CleanService
        extends BaseService<CleanService.Input, ServiceSuccess> {

    public static class Input extends Validated {
        @Override
        public void validate() throws ServiceFailure {}
    }

    private final DocumentStore documentStore;

    public CleanService(final DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    @Override
    public Class<Input> getInputClass() {
        return Input.class;
    }

    @Override
    public ServiceSuccess handle(final Input input) throws ServiceFailure {
        try {
            this.documentStore.clean();
            return new ServiceSuccess();
        } catch (DatabaseException e) {
            // this is unlikely, why would this be a checked exception?
            throw new ServiceFailure("database error");
        }
    }

}