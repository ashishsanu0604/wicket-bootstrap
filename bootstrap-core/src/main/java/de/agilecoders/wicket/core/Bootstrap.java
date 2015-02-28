package de.agilecoders.wicket.core;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.BootstrapResourcesBehavior;
import de.agilecoders.wicket.core.settings.BootstrapResourceAppender;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import de.agilecoders.wicket.jquery.WicketJquerySelectors;
import de.agilecoders.wicket.webjars.WicketWebjars;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.IMarkupSettings;
import org.apache.wicket.util.lang.Args;

/**
 * #### Description
 * <p/>
 * This is the bootstrap initializer class, you've to call `install` (with or without custom settings) to
 * enable wicket-bootstrap in your project. wicket-bootstrap sets some wicket settings depending on your
 * custom settings or if non provided depending on default settings. These are: `setStripWicketTags(true)` (always),
 * `getPackageResourceGuard().addPattern(...)` (if `settings.updateSecurityManager()` is true) and
 * `getComponentInstantiationListeners().add(new BootstrapResourceAppender())` (if `settings.autoAppendResources()`
 * is true).
 * `Bootstrap` initializes and uses wicket-webjars to load its web resources, therefor wicket-webjars will be initialized
 * when calling `install` too (only if `settings.useWebjars()` returns true). If you use your own resources or
 * the cdn resources, wicket-webjars won't be initialized. wicket-webjars can be initialized by calling
 * `WicketWebjars.install(yourWicketApplication)` manually if needed.
 * <p/>
 * #### Usage
 * <p/>
 * minimal version:
 * <p/>
 * ```java
 * Bootstrap.install(yourWicketApplication);
 * ```
 * <p/>
 * with custom settings:
 * <p/>
 * ```java
 * final IBootstrapSettings settings = new BootstrapSettings();
 * settings.useCdnResources(true);
 * Bootstrap.install(yourWicketApplication, settings);
 * ```
 *
 * @author Michael Haitz <michael.haitz@agilecoders.de>
 */
public final class Bootstrap {

    /**
     * The {@link MetaDataKey} used to retrieve the {@link IBootstrapSettings} from the Wicket {@link Appendable}.
     */
    private static final MetaDataKey<IBootstrapSettings> BOOTSTRAP_SETTINGS_METADATA_KEY = new MetaDataKey<IBootstrapSettings>() {
    };

    /**
     * Construct.
     */
    private Bootstrap() {
        throw new UnsupportedOperationException();
    }

    /**
     * Installs given settings to given application. Duplicated calls to `install` will be ignored.
     *
     * @param app      The current application
     * @param settings The settings to use
     * @throws java.lang.IllegalArgumentException if given application is null
     */
    public static void install(final Application app, IBootstrapSettings settings) {
        Args.notNull(app, "app");

        if (getSettings(app) == null) {
            if (settings == null) {
                settings = new BootstrapSettings();
            }

            app.setMetaData(BOOTSTRAP_SETTINGS_METADATA_KEY, settings);

            if (!WicketJquerySelectors.isInstalled(app)) {
                WicketJquerySelectors.install(app);
            }

            if (settings.useWebjars() && app instanceof WebApplication) {
                WicketWebjars.install((WebApplication) app);
            }

            if (settings.updateSecurityManager()) {
                updateSecurityManager(app);
            }

            if (settings.autoAppendResources()) {
                app.getComponentInstantiationListeners().add(new BootstrapResourceAppender());
            }

            configureMarkupSettings(app);
        }
    }

    /**
     * Installs default bootstrap settings to given application. Duplicated calls to `install` will be ignored.
     *
     * @param app The current application
     * @throws java.lang.IllegalArgumentException if given application is null
     */
    public static void install(final Application app) {
        install(app, null);
    }

    /**
     * configure wicket markup settings
     *
     * @param application current application
     */
    private static void configureMarkupSettings(Application application) {
        IMarkupSettings markupSettings = application.getMarkupSettings();

        // wicket markup leads to ui problems because css selectors doesn't match.
        markupSettings.setStripWicketTags(true);
    }

    /**
     * updates the security manager to allow fonts and less files if necessary.
     *
     * @param app The current application
     */
    private static void updateSecurityManager(final Application app) {
        final IPackageResourceGuard packageResourceGuard = app.getResourceSettings().getPackageResourceGuard();

        if (packageResourceGuard instanceof SecurePackageResourceGuard) {
            SecurePackageResourceGuard guard = (SecurePackageResourceGuard) packageResourceGuard;
            guard.addPattern("+*.woff");
            guard.addPattern("+*.woff2");            
            guard.addPattern("+*.eot");
            guard.addPattern("+*.svg");
            guard.addPattern("+*.ttf");
            guard.addPattern("+*.css.map");
        }
    }

    /**
     * returns the {@link IBootstrapSettings} which are assigned to given application
     *
     * @param app The current application
     * @return assigned {@link IBootstrapSettings}
     * @throws java.lang.IllegalArgumentException if given application is null
     */
    public static IBootstrapSettings getSettings(final Application app) {
        return Args.notNull(app, "app").getMetaData(BOOTSTRAP_SETTINGS_METADATA_KEY);
    }

    /**
     * returns the {@link IBootstrapSettings} which are assigned to current application
     *
     * @return assigned {@link IBootstrapSettings}
     * @throws IllegalStateException if there's no application bound to current thread
     */
    public static IBootstrapSettings getSettings() {
        if (Application.exists()) {
            return getSettings(Application.get());
        }

        throw new IllegalStateException("there is no active application assigned to this thread.");
    }

    /**
     * renders all core bootstrap resource references
     *
     * @param component current component
     * @param response  The current {@link IHeaderResponse}
     * @throws java.lang.IllegalArgumentException if given component or response is null.
     * @throws IllegalStateException              if there's no application bound to current thread
     */
    public static void renderHead(final Component component, final IHeaderResponse response) {
        Args.notNull(component, "component");
        Args.notNull(response, "response");

        BootstrapResourcesBehavior.instance().renderHead(getSettings(component.getApplication()), response);
    }

    /**
     * renders all core bootstrap resource references
     *
     * @param response The current {@link IHeaderResponse}
     * @throws java.lang.IllegalArgumentException if given response is null
     * @throws IllegalStateException              if there's no application bound to current thread
     */
    public static void renderHead(final IHeaderResponse response) {
        Args.notNull(response, "response");

        BootstrapResourcesBehavior.instance().renderHead(getSettings(Application.get()), response);
    }
}
