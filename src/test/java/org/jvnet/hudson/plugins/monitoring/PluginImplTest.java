package org.jvnet.hudson.plugins.monitoring;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.util.PluginServletFilter;
import jakarta.servlet.FilterConfig;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class PluginImplTest {

    @Test
    void stopRemovesAndDestroysTheRegisteredFilter(JenkinsRule rule) throws Exception {
        PluginImpl plugin = Jenkins.get().getPlugin(PluginImpl.class);
        assertNotNull(plugin);
        HudsonMonitoringFilter filter = plugin.getFilter();
        assertNotNull(filter);
        assertTrue(PluginServletFilter.hasFilter(filter));

        plugin.stop();

        assertFalse(PluginServletFilter.hasFilter(filter));
        assertNull(plugin.getFilter());

        // stop may be reached again during Jenkins cleanup; it must not destroy the filter twice.
        plugin.stop();
    }

    @Test
    void removesFilterBeforeDestroyingIt(JenkinsRule rule) throws Exception {
        TrackingFilter filter = new TrackingFilter();
        PluginServletFilter.addFilter(filter);

        PluginImpl.stopFilter(filter);

        assertTrue(filter.destroyed);
        assertFalse(filter.wasRegisteredDuringDestroy);
        assertFalse(PluginServletFilter.hasFilter(filter));
    }

    private static final class TrackingFilter extends HudsonMonitoringFilter {
        private boolean destroyed;
        private boolean wasRegisteredDuringDestroy;

        @Override
        public void init(FilterConfig config) {
            // This test filter only tracks the registration and destroy lifecycle.
        }

        @Override
        public void destroy() {
            wasRegisteredDuringDestroy = PluginServletFilter.hasFilter(this);
            destroyed = true;
        }
    }
}
