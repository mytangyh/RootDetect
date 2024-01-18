#include <jni.h>
#include <cstring>
#include <cerrno>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <sys/socket.h>
#include <cstdio>
#include <dirent.h>
#include <fstream>
#include <string>
#include <iostream>
#include <sys/ptrace.h>

using namespace std;

class AntiFrida {
public:

    /**
     *  检查指定端口是否开启
     * @return
     */
    bool check_frida_server() {
        struct sockaddr_in sa;
        memset(&sa, 0, sizeof(sa));
        sa.sin_family = AF_INET;
        sa.sin_port = htons(27042);
        inet_aton("127.0.0.1", &(sa.sin_addr));

        int sock = socket(AF_INET, SOCK_STREAM, 0);
        if (connect(sock, reinterpret_cast<struct sockaddr*>(&sa), sizeof sa) != -1) {
            close(sock);
            return true;
        }

        close(sock);
        return false;
    }

    /**
     * 检查内存映射 maps
     * @return
     */
    bool check_proc_maps() {
        ifstream maps("/proc/self/maps");
        string line;

        while (getline(maps, line)) {
            if (line.find("frida") != string::npos) {
                maps.close();
                return true;
            }
        }

        return false;
    }

    /**
     * 检查 fd
     * @return
     */
    bool check_proc_fd() {
        DIR* dir = opendir("/proc/self/fd/");
        if (!dir) {
            // 处理打开目录失败的情况
            return false;
        }

        bool ret = false;
        struct dirent* entry;
        char link_name[100];
        char buf[100];

        while ((entry = readdir(dir))) {
            switch (entry->d_type) {
                case DT_LNK: {
                    snprintf(link_name, sizeof(link_name), "%s/%s", "/proc/self/fd/", entry->d_name);
                    ssize_t len = readlink(link_name, buf, sizeof(buf) - 1);
                    if (len != -1) {
                        buf[len] = '\0';  // 确保字符串以 null 结尾
                        if (strstr(buf, "frida") || strstr(buf, "gum-js-loop") ||
                            strstr(buf, "gmain") || strstr(buf, "-gadget") || strstr(buf, "linjector")) {
                            ret = true;
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }

        closedir(dir);
        return ret;
    }

    /**
     *  检查dbus 遍历所有端口 耗时
     * @return
     */
    bool check_server_dbus() {

        int sock;
        struct sockaddr_in sa;
        bzero(&sa, sizeof(sa));
        sa.sin_family = AF_INET;
        inet_aton("127.0.0.1", &(sa.sin_addr));
        if (inet_pton(AF_INET, "127.0.0.1", &sa.sin_addr) < 0) {    //set ip address
            return false;
        }

        char res[7];
        for (int i = 10000; i <= 65535; i++) {
            sock = socket(AF_INET, SOCK_STREAM, 0);
            if (sock == -1) {
                continue;
            }

            sa.sin_port = htons(i);
            if (connect(sock, (struct sockaddr *) &sa, sizeof(sa)) >= 0) {
                memset(res, 0, 7);
                send(sock, "\x00", 1, 0);
                send(sock, "AUTH\r\n", 6, 0);
                usleep(1);    // Give it some time to answer

                ssize_t received = recv(sock, res, 6, MSG_DONTWAIT);
                if (received != -1 && strncmp(res, "REJECT", 6) == 0) {
                    close(sock);
                    return true;
                }
            }

            close(sock);
        }
        return false;
    }
};


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_lib_Native_00024Companion_checkFrida(JNIEnv *env, jobject thiz) {
    AntiFrida antiFrida;
    bool res = antiFrida.check_frida_server()||antiFrida.check_proc_maps()||antiFrida.check_proc_fd();
    return res;
}


