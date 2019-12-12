package org.leadpony.justify.internal.keyword.assertion;

import java.time.ZonedDateTime;
import javax.json.JsonValue;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * Assertion defined in ajv JSON extension proposal.
 *
 * @author JoaoCamposFrom94
 */
@KeywordType("formatMaximum")
@Spec(SpecVersion.AJV_EXTENSION_PROPOSAL)
@Spec(SpecVersion.DRAFT_07)
@Spec(SpecVersion.AJV_EXTENSION_PROPOSAL)
public class FormatMaximum extends AbstractTemporalAssertion {

    public FormatMaximum(JsonValue json, final String date) {
        super(json, date);
    }

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        return (KeywordMapper.FromString) FormatMaximum::new;
    }

    @Override
    public boolean testValue(String value) {
        try {
            ZonedDateTime contextDateTime = parseDate(value);
            ZonedDateTime schemaDateTime = parseDate(this.date);

            return contextDateTime.isBefore(schemaDateTime) || contextDateTime.isEqual(schemaDateTime);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected Problem createProblem(ProblemBuilder builder) {
        builder.withParameter("formatMaximum", this.date)
            .withMessage(Message.FORMAT_DATE_TIME);
        return builder.build();
    }

    @Override
    protected Problem createNegatedProblem(ProblemBuilder builder) {
        builder.withParameter("formatMaximum", this.date)
            .withMessage(Message.FORMAT_DATE_TIME);
        return builder.build();
    }
}
