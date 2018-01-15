package net.deckerego.docidx.repository;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Repository;

import java.util.Date;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.*;

@Repository
public class IndexStatsRepository {
    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    public Date documentLastUpdated() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .withSearchType(SearchType.DEFAULT)
                .withIndices("docidx").withTypes("fileentry")
                .addAggregation(max("indexLastUpdated").field("indexUpdated"))
                .build();

        return elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Date>() {
            @Override
            public Date extract(SearchResponse response) {
                Max agg = response.getAggregations().get("indexLastUpdated");
                double rawValue = agg.getValue();
                return new Date((long) rawValue);
            }
        });
    }

    public Date tagTemplateLastUpdated() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .withSearchType(SearchType.DEFAULT)
                .withIndices("docidx").withTypes("tagtemplate")
                .addAggregation(max("indexLastUpdated").field("indexUpdated"))
                .build();

        return elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Date>() {
            @Override
            public Date extract(SearchResponse response) {
                Max agg = response.getAggregations().get("indexLastUpdated");
                double rawValue = agg.getValue();
                return new Date((long) rawValue);
            }
        });
    }
}
