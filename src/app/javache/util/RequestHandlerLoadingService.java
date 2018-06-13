package app.javache.util;

import app.javache.WebConstants;
import app.javache.api.RequestHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class RequestHandlerLoadingService {

    private static final String LIB_FOLDER_PATH = WebConstants.SERVER_ROOT_FOLDER_PATH.replace("app/javache/", "lib/");

    private Set<RequestHandler> requestHandlers;

    public RequestHandlerLoadingService() {
    }

    public Set<RequestHandler> getRequestHandlers() {
        return Collections.unmodifiableSet(this.requestHandlers);
    }

    private String getFileNameWithoutExtension(File file) {
        return file.getName().substring(0, file.getName().indexOf(".")).toString();
    }

    private boolean isJarFile(File file) {
        return file.isFile() && file.getName().endsWith(".jar");
    }

    private void loadRequestHandler() {

    }

    private void loadJarFile(JarFile jarFile) {
        Enumeration<JarEntry> jarFileEntries = jarFile.entries();

        while (jarFileEntries.hasMoreElements()) {
            JarEntry currentEntry = jarFileEntries.nextElement();

            System.out.println(currentEntry.getName());
        }
    }

    private void loadLibraryFiles(Set<String> requestHandlerPriority) throws IOException {
        File libraryFolder = new File(LIB_FOLDER_PATH);

        if (!libraryFolder.exists() || !libraryFolder.isDirectory()) {
            throw new IllegalArgumentException("Library Folder is not a folder or does not exist.");
        }


        List<File> allJarFiles = Arrays.stream(libraryFolder.listFiles()).filter(f -> this.isJarFile(f)).collect(Collectors.toList());

        for (String currentRequestHandlerName : requestHandlerPriority) {
            File currentJarFile = allJarFiles
                    .stream().filter(f -> this.getFileNameWithoutExtension(f).equals(currentRequestHandlerName)).findFirst().orElse(null);

            if (currentJarFile != null) {
                JarFile jarFile = new JarFile(currentJarFile.getCanonicalPath());
                this.loadJarFile(jarFile);
            }
        }
    }

    public void loadRequestHandlers(Set<String> requestHandlerPriority) {
        this.requestHandlers = new LinkedHashSet<>();

        try {
            this.loadLibraryFiles(requestHandlerPriority);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
