/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluemap.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * An API to control the running instance of BlueMap.
 * <p>This API is thread-save, so you <b>can</b> use it async, off the main-server-thread, to save performance!</p>
 */
public abstract class BlueMapAPI {

    @SuppressWarnings("unused")
    private static final String VERSION, GIT_HASH, GIT_CLEAN;
    static {
        String version = "DEV", gitHash = "DEV", gitClean = "DEV";
        URL url = BlueMapAPI.class.getResource("/de/bluecolored/bluemap/api/version");
        if (url != null) {
            try (InputStream in = url.openStream(); Scanner scanner = new Scanner(in)) {
                version = scanner.nextLine();
                gitHash = scanner.nextLine();
                gitClean = scanner.nextLine();
            } catch (IOException ex) {
                System.err.println("Failed to load version from resources!");
                ex.printStackTrace();
            }
        }

        if (version.equals("${version}")) version = "DEV";
        if (gitHash.equals("${gitHash}")) version = "DEV";
        if (gitClean.equals("${gitClean}")) version = "DEV";

        VERSION = version;
        GIT_HASH = gitHash;
        GIT_CLEAN = gitClean;
    }

    private static BlueMapAPI instance;

    private static final Collection<Consumer<BlueMapAPI>> onEnableConsumers = new HashSet<>(2);
    private static final Collection<Consumer<BlueMapAPI>> onDisableConsumers = new HashSet<>(2);

    /**
     * Getter for the {@link RenderManager}.
     * @return the {@link RenderManager}
     */
    public abstract RenderManager getRenderManager();

    /**
     * Getter for the {@link WebApp}.
     * @return the {@link WebApp}
     */
    public abstract WebApp getWebApp();

    /**
     * Getter for all {@link BlueMapMap}s loaded by BlueMap.
     * @return an unmodifiable collection of all loaded {@link BlueMapMap}s
     */
    public abstract Collection<BlueMapMap> getMaps();

    /**
     * Getter for all {@link BlueMapWorld}s loaded by BlueMap.
     * @return an unmodifiable collection of all loaded {@link BlueMapWorld}s
     */
    public abstract Collection<BlueMapWorld> getWorlds();

    /**
     * Getter for a {@link BlueMapWorld} loaded by BlueMap.
     *
     * @param world Any object that BlueMap can use to identify a world.
     *              <p>
     *              <b>This could be:</b>
     *              <ul>
     *                  <li>A {@link String} that is the id of the world</li>
     *                  <li>A {@link Path} that is the path to the world-folder</li>
     *                  <li>A Resource-Key object, {@link UUID} or anything that your platform uses to identify worlds</li>
     *                  <li>The actual world-object, any object directly representing the a world on your platform</li>
     *              </ul>
     *              <i>("Platform" here stands for the mod/plugin-loader or server-implementation you are using,
     *              e.g. Spigot, Forge, Fabric or Sponge)</i>
     *              </p>
     * @return an {@link Optional} with the {@link BlueMapWorld} if it exists
     */
    public abstract Optional<BlueMapWorld> getWorld(Object world);

    /**
     * Getter for a {@link BlueMapMap} loaded by BlueMap with the given id.
     * @param id the map id (equivalent to the id configured in BlueMap's config
     * @return an {@link Optional} with the {@link BlueMapMap} if it exists
     */
    public abstract Optional<BlueMapMap> getMap(String id);

    /**
     * Getter for the installed BlueMap version
     * @return the version-string
     */
    public abstract String getBlueMapVersion();

    /**
     * Getter for the installed BlueMapAPI version
     * @return the version-string
     */
    public String getAPIVersion() {
        return VERSION;
    }

    /**
     * Returns an instance of {@link BlueMapAPI} if it is currently enabled, else an empty {@link Optional} is returned.
     * @return an {@link Optional}&lt;{@link BlueMapAPI}&gt;
     */
    public static synchronized Optional<BlueMapAPI> getInstance() {
        return Optional.ofNullable(instance);
    }

    /**
     * Registers a {@link Consumer} that will be called every time BlueMap has just been loaded and started and the API is ready to use.<br>
     * If {@link BlueMapAPI} is already enabled when this listener is registered the consumer will be called immediately <i>(once, on the same thread)</i>!
     * <p><b>The {@link Consumer} can be called multiple times if BlueMap disables and enables again, e.g. if BlueMap gets reloaded!</b></p>
     * <p><i>(Note: The consumer will likely be called asynchronously, <b>not</b> on the server-thread!)</i></p>
     * <p>Remember to unregister the consumer when you no longer need it using {@link #unregisterListener(Consumer)}.</p>
     * @param consumer the {@link Consumer}
     */
    public static synchronized void onEnable(Consumer<BlueMapAPI> consumer) {
        onEnableConsumers.add(consumer);
        if (BlueMapAPI.instance != null) consumer.accept(BlueMapAPI.instance);
    }

    /**
     * Registers a {@link Consumer} that will be called every time <b>before</b> BlueMap is being unloaded and stopped, after the consumer returns the API is no longer usable!<br>
     * Unlike with {@link BlueMapAPI#onEnable(Consumer)}, if {@link BlueMapAPI} is not enabled when this listener is registered the consumer will <b>not</b> be called.
     * <p><b>The {@link Consumer} can be called multiple times if BlueMap disables and enables again, e.g. if BlueMap gets reloaded!</b></p>
     * <p><i>(Note: The consumer will likely be called asynchronously, <b>not</b> on the server-thread!)</i></p>
     * <p>Remember to unregister the consumer when you no longer need it using {@link #unregisterListener(Consumer)}.</p>
     * @param consumer the {@link Consumer}
     */
    public static synchronized void onDisable(Consumer<BlueMapAPI> consumer) {
        onDisableConsumers.add(consumer);
    }

    /**
     * Removes a {@link Consumer} that has been registered using {@link #onEnable(Consumer)} or {@link #onDisable(Consumer)}.
     * @param consumer the {@link Consumer} instance that has been registered previously
     * @return <code>true</code> if a listener was removed as a result of this call
     */
    public static synchronized boolean unregisterListener(Consumer<BlueMapAPI> consumer) {
        return onEnableConsumers.remove(consumer) | onDisableConsumers.remove(consumer);
    }

    /**
     * Used by BlueMap to register the API and call the listeners properly.
     * @param instance the {@link BlueMapAPI}-instance
     * @return <code>true</code> if the instance has been registered, <code>false</code> if there already was an instance registered
     * @throws ExecutionException if a listener threw an exception during the registration
     */
    protected static synchronized boolean registerInstance(BlueMapAPI instance) throws ExecutionException {
        if (BlueMapAPI.instance != null) return false;

        BlueMapAPI.instance = instance;

        List<Throwable> thrownExceptions = new ArrayList<>(0);

        for (Consumer<BlueMapAPI> listener : BlueMapAPI.onEnableConsumers) {
            try {
                listener.accept(BlueMapAPI.instance);
            } catch (Throwable ex) {
                thrownExceptions.add(ex);
            }
        }

        if (!thrownExceptions.isEmpty()) {
            ExecutionException ex = new ExecutionException(thrownExceptions.get(0));
            for (int i = 1; i < thrownExceptions.size(); i++) {
                ex.addSuppressed(thrownExceptions.get(i));
            }
            throw ex;
        }

        return true;
    }

    /**
     * Used by BlueMap to unregister the API and call the listeners properly.
     * @param instance the {@link BlueMapAPI} instance
     * @return <code>true</code> if the instance was unregistered, <code>false</code> if there was no or an other instance registered
     * @throws ExecutionException if a listener threw an exception during the un-registration
     */
    protected static synchronized boolean unregisterInstance(BlueMapAPI instance) throws ExecutionException {
        if (BlueMapAPI.instance != instance) return false;

        List<Exception> thrownExceptions = new ArrayList<>(0);

        for (Consumer<BlueMapAPI> listener : BlueMapAPI.onDisableConsumers) {
            try {
                listener.accept(BlueMapAPI.instance);
            } catch (Exception ex) {
                thrownExceptions.add(ex);
            }
        }

        BlueMapAPI.instance = null;

        if (!thrownExceptions.isEmpty()) {
            ExecutionException ex = new ExecutionException(thrownExceptions.get(0));
            for (int i = 1; i < thrownExceptions.size(); i++) {
                ex.addSuppressed(thrownExceptions.get(i));
            }
            throw ex;
        }

        return true;
    }
}
