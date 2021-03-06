package app.javache.util;

import app.javache.WebConstants;
import app.javache.api.RequestHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class RequestHandlerLoadingService {

    private static final String LIB_FOLDER_PATH = WebConstants.SERVER_ROOT_FOLDER_PATH + "lib/";

    private Set<RequestHandler> requestHandlers;

    public RequestHandlerLoadingService() {
    }

    public Set<RequestHandler> getRequestHandlers() {
        return Collections.unmodifiableSet(this.requestHandlers);
    }

    private String getFileNameWithoutExtension(File file) {
        return file.getName().substring(0, file.getName().indexOf("."));
    }

    private boolean isJarFile(File file) {
        return file.isFile() && file.getName().endsWith(".jar");
    }

    private void loadRequestHandler(Class<?> requestHandlerClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        RequestHandler requestHandlerObject = (RequestHandler) requestHandlerClass.getDeclaredConstructor(String.class).newInstance(WebConstants.SERVER_ROOT_FOLDER_PATH);

        this.requestHandlers.add(requestHandlerObject);
    }

    private void loadJarFile(String cannonicalPath, JarFile jarFile) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Enumeration<JarEntry> jarFileEntries = jarFile.entries();

        while (jarFileEntries.hasMoreElements()) {
            JarEntry currentEntry = jarFileEntries.nextElement();

            if (!currentEntry.isDirectory() && currentEntry.getRealName().endsWith(".class")) {
                URL[] urls = new URL[]{new URL("jar:file:" + cannonicalPath + "!/")};

                URLClassLoader urlClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

                Thread.currentThread().setContextClassLoader(urlClassLoader);

                String className = currentEntry.getName().replace(".class", "").replace("/", ".");

                Class currentClassFile = urlClassLoader.loadClass(className);

                if (RequestHandler.class.isAssignableFrom(currentClassFile)) {
                    this.loadRequestHandler(currentClassFile);
                }
            }
        }
    }

    private void loadLibraryFiles(Set<String> requestHandlerPriority) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        File libraryFolder = new File(LIB_FOLDER_PATH);

        if (!libraryFolder.exists() || !libraryFolder.isDirectory()) {
            throw new IllegalArgumentException("Library Folder is not a folder or does not exist.");
        }


        List<File> allJarFiles = Arrays.stream(libraryFolder.listFiles()).filter(f -> this.isJarFile(f)).collect(Collectors.toList());

        for (String currentRequestHandlerName : requestHandlerPriority) {
            File jarFile = allJarFiles
                    .stream().filter(f -> this.getFileNameWithoutExtension(f).equals(currentRequestHandlerName)).findFirst().orElse(null);

            if (jarFile != null) {
                JarFile fileAsJar = new JarFile(jarFile.getCanonicalPath());
                this.loadJarFile(jarFile.getCanonicalPath(), fileAsJar);
            }
        }
    }

    public void loadRequestHandlers(Set<String> requestHandlerPriority) throws NoSuchMethodException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        this.requestHandlers = new LinkedHashSet<>();

        this.loadLibraryFiles(requestHandlerPriority);

    }
}
