package co.deepthought.quickscan.service;

import co.deepthought.quickscan.store.Document;
import co.deepthought.quickscan.store.DocumentStore;
import co.deepthought.quickscan.store.Score;

import java.sql.SQLException;
import java.util.Map;

public class UpsertService
        extends BaseService<UpsertService.Input, ServiceSuccess> {

    public static class Input extends Validated {

        public String documentId;
        public String resultId;
        public String shardId;
        public String[] tags;
        public Map<String, Double> fields;
        public Map<String, Double> scores;
        public Input() {}

        @Override
        public void validate() throws ServiceFailure {
            this.validateNonNull(this.documentId, "documentId");
            this.validateNonNull(this.documentId, "resultId");
            this.validateNonNull(this.documentId, "shardId");
            for(final String tag : this.tags) {
                this.validateNonNull(tag, "tags[]");
            }
            for(final Double field : this.fields.values()) {
                this.validateNonNull(field, "fields[]");
            }
            for(final Double score : this.scores.values()) {
                this.validateNonNull(score, "scores[]");
                if(score < 0 || score > 1) {
                    throw new ServiceFailure("scores[] must on [0,1]");
                }
            }
        }
    }

    private final DocumentStore documentStore;

    public UpsertService(final DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    @Override
    public Class<Input> getInputClass() {
        return Input.class;
    }

    @Override
    public ServiceSuccess handle(final Input input) throws ServiceFailure {
        try {
            this.documentStore.deleteById(input.documentId);

            final Document document = this.documentStore.createDocument(input.documentId, input.resultId, input.shardId);
            for(final String tag : input.tags) {
                document.addTag(tag);
            }
            for(final Map.Entry<String, Double> field : input.fields.entrySet()) {
                document.addField(field.getKey(), field.getValue());
            }
            for(final Map.Entry<String, Double> score : input.scores.entrySet()) {
                document.addScore(score.getKey(), score.getValue());
            }
            this.documentStore.persistDocument(document);
            return new ServiceSuccess();
        } catch (SQLException e) {
            // this is unlikely, why would this be a checked exception?
            throw new ServiceFailure("database error");
        }

    }

}