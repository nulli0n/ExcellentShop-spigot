package su.nightexpress.excellentshop.shop.formatter;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductFormatter<P extends Product> {

    private static class ConditionRule<P extends Product> {

        final String              tag;
        final String              openTag;
        final Pattern             pattern;
        final DisplayCondition<P> condition;

        ConditionRule(@NonNull String tag, @NonNull DisplayCondition<P> condition) {
            this.tag = tag;
            this.openTag = "<if_" + tag + ">";

            this.pattern = Pattern.compile("<if_" + tag + ">(.*?)</if_" + tag + ">");
            this.condition = condition;
        }
    }

    private static class VariableRule<P extends Product> {

        final String              placeholder;
        final VariableReplacer<P> replacer;

        VariableRule(@NonNull String tag, @NonNull VariableReplacer<P> replacer) {
            this.placeholder = "{" + tag + "}";
            this.replacer = replacer;
        }
    }

    private final List<ConditionRule<P>> conditions = new ArrayList<>();
    private final List<VariableRule<P>>  variables  = new ArrayList<>();

    public void registerCondition(@NonNull String tag, @NonNull DisplayCondition<P> condition) {
        this.conditions.add(new ConditionRule<>(tag, condition));
    }

    public void registerVariable(@NonNull String tag, @NonNull VariableReplacer<P> replacer) {
        this.variables.add(new VariableRule<>(tag, replacer));
    }

    @NonNull
    public String formatLine(@NonNull String line, @NonNull P product, @NonNull Player player) {
        if (line.isEmpty()) return line;

        // Fast check #1
        if (line.contains("<if_")) {
            for (ConditionRule<P> rule : this.conditions) {
                // Fast check #2
                if (!line.contains(rule.openTag)) continue;

                boolean isConditionMet = rule.condition.check(product, player);
                Matcher matcher = rule.pattern.matcher(line);

                StringBuilder sb = new StringBuilder();
                while (matcher.find()) {
                    if (isConditionMet) {
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1)));
                    }
                    else {
                        matcher.appendReplacement(sb, "");
                    }
                }
                matcher.appendTail(sb);
                line = sb.toString();
            }
        }

        // Fast check #3
        if (line.contains("{")) {
            for (VariableRule<P> rule : variables) {
                // Fast check #4
                if (line.contains(rule.placeholder)) {
                    String replacementValue = rule.replacer.replace(product, player);
                    line = line.replace(rule.placeholder, replacementValue);
                }
            }
        }

        return line.trim();
    }
}