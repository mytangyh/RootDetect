#include <jni.h>
#include <cstring>
#include <cerrno>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <sys/socket.h>
#include <cstdio>
#include <dirent.h>
#include <android/log.h>
#include <fstream>
#include <string>


using namespace std;

#define LOG_TAG "MyApp"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

class AntiFrida {
public:
    /**
 * 当前函数执行较耗时，需要放入子线程进行
 * @return  检查到frida痕迹，返回true，否则返回false
 */
    bool check_Server_func() {
        LOGI("in AntiFrida::check_Server_func");

        int sock;
        struct sockaddr_in sa;
        bzero(&sa, sizeof(sa));
        sa.sin_family = AF_INET;
        inet_aton("127.0.0.1", &(sa.sin_addr));
        if (inet_pton(AF_INET, "127.0.0.1", &sa.sin_addr) < 0) {    //set ip address
            LOGE(" %s - %d  error:%s", __FILE__, __LINE__, strerror(errno));
            return false;
        }

        char res[7];
        for (int i = 10000; i <= 65535; i++) {
            sock = socket(AF_INET, SOCK_STREAM, 0);
            if (sock == -1) {
                LOGI("test socket fail:%s", strerror(errno));
                continue;
            }

            sa.sin_port = htons(i);
            LOGI("check_Server_func test connect port:%d", i);
            if (connect(sock, (struct sockaddr *) &sa, sizeof(sa)) >= 0) {
                LOGI("check_Server_func connect 127.0.0.1:%d", i);
                memset(res, 0, 7);
                send(sock, "\x00", 1, 0);
                send(sock, "AUTH\r\n", 6, 0);
                usleep(1);    // Give it some time to answer

                ssize_t received = recv(sock, res, 6, MSG_DONTWAIT);
                if (received != -1 && strncmp(res, "REJECT", 6) == 0) {
                    close(sock);
                    LOGI("check_Server find frida for port:%d", i);
                    return true;
                }
            }

            close(sock);
        }
        return false;
    }

    bool check_fd() {
        LOGI("in AntiFrida::check_fd");
        DIR *dir = NULL;
        struct dirent *entry;
        char link_name[100];
        char buf[100];
        bool ret = false;
        if ((dir = opendir("/proc/self/fd/")) == NULL) {
            LOGE(" %s - %d  error:%s", __FILE__, __LINE__, strerror(errno));
        } else {
            entry = readdir(dir);
            while (entry) {
                switch (entry->d_type) {
                    case DT_LNK:
                        sprintf(link_name, "%s/%s", "/proc/self/fd/", entry->d_name);
                        readlink(link_name, buf, sizeof(buf));
                        if (strstr(buf, "frida") || strstr(buf, "gum-js-loop") ||
                            strstr(buf, "gmain") ||
                            strstr(buf, "-gadget") || strstr(buf, "linjector")) {
                            LOGI("check_fd -> find frida:%s", buf);
                            ret = true;
                        }
                        break;
                    default:
                        break;
                }
                entry = readdir(dir);
            }
        }
        closedir(dir);
        return ret;

    }

    char *get_frida_server_status() {
        struct sockaddr_in sa;
        memset(&sa, 0, sizeof(sa));
        sa.sin_family = AF_INET;
        sa.sin_port = htons(27042);
        inet_aton("127.0.0.1", &(sa.sin_addr));

        int sock = socket(AF_INET, SOCK_STREAM, 0);
        if (connect(sock, (struct sockaddr *) &sa, sizeof sa) != -1) {
            close(sock);
            return strdup("Frida server is running.");
        }

        close(sock);
        return strdup("Frida server is not running.");
    }


    string isFridaInjected() {
        ifstream maps("/proc/self/maps");
        string line;

        while (getline(maps, line)) {
            if (line.find("frida") != string::npos) {
                maps.close();
                return "true+++";
            }
        }

        return "false";
    }
};






