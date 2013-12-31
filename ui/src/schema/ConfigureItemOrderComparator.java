package schema;

import java.util.Comparator;

public class ConfigureItemOrderComparator implements Comparator<ConfigureItem> {
    public int compare(ConfigureItem o1, ConfigureItem o2) {
        return o1.getOrder() - o2.getOrder();
    }
}
