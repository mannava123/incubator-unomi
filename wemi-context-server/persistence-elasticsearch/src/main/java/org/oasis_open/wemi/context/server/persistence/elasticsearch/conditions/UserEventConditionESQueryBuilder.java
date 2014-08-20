package org.oasis_open.wemi.context.server.persistence.elasticsearch.conditions;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.oasis_open.wemi.context.server.api.Session;
import org.oasis_open.wemi.context.server.api.conditions.Condition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toto on 11/08/14.
 */
public class UserEventConditionESQueryBuilder implements ESQueryBuilder {
    public UserEventConditionESQueryBuilder() {
    }

    public FilterBuilder buildFilter(Condition condition, ConditionESQueryBuilderDispatcher dispatcher) {
        String numberOfDays = (String) condition.getParameterValues().get("numberOfDays");
        String occursIn = (String) condition.getParameterValues().get("eventOccurIn");
        final Condition eventCondition = (Condition) condition.getParameterValues().get("eventCondition");

        Session targetSession = (Session) condition.getParameterValues().get("target");
        if (targetSession == null) {
            if (numberOfDays != null) {
                return FilterBuilders.rangeFilter("properties." + (String) eventCondition.getParameterValues().get("generatedPropertyKey"))
                        .gt("now-" + numberOfDays + "d")
                        .lt("now");
            } else {
                return FilterBuilders.existsFilter("properties." + (String) eventCondition.getParameterValues().get("generatedPropertyKey"));
            }
        } else {
            List<FilterBuilder> l = new ArrayList<FilterBuilder>();
            l.add(dispatcher.buildFilter(eventCondition));
            if (occursIn != null && (occursIn.equals("session") || occursIn.equals("last"))) {
                l.add(FilterBuilders.termFilter("sessionId", targetSession.getItemId()));
            } else {
                l.add(FilterBuilders.termFilter("userId", targetSession.getUserId()));
            }
            if (numberOfDays != null && !numberOfDays.equals("")) {
                l.add(FilterBuilders.rangeFilter("timeStamp")
                        .gt("now-" + numberOfDays + "d")
                        .lt("now"));
            }
            return FilterBuilders.andFilter(l.toArray(new FilterBuilder[l.size()]));
        }
    }
}