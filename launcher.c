#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <libgen.h>

int main(int argc, char **argv) {
    char exe_path[PATH_MAX];
    ssize_t n = readlink("/proc/self/exe", exe_path, sizeof(exe_path) - 1);
    if (n < 0) {
        perror("readlink");
        return 1;
    }
    exe_path[n] = '\0';

    char dir_buf[PATH_MAX];
    strncpy(dir_buf, exe_path, sizeof(dir_buf) - 1);
    char *dir = dirname(dir_buf);

    char jar[PATH_MAX];
    snprintf(jar, sizeof(jar), "%s/dist/healthfirst_pims.jar", dir);
    if (access(jar, R_OK) != 0) {
        snprintf(jar, sizeof(jar), "%s/healthfirst_pims.jar", dir);
    }

    char **args = calloc((size_t) argc + 4, sizeof(char *));
    if (!args) {
        return 1;
    }
    args[0] = "java";
    args[1] = "-jar";
    args[2] = jar;
    for (int i = 1; i < argc; i++) {
        args[i + 2] = argv[i];
    }
    execvp("java", args);
    perror("Unable to start Java. Install a JDK 17+ runtime and try again");
    return 1;
}
