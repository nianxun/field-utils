# centos7.6升级openssl1.1.1.v和openssh9.6p
## 下载必要安装包
``` shell
yum install yum-utils -y
repotrack -p /root/upload-ssh install perl-CPAN perl-IPC-Cmd gcc gcc-c++ pam-devel libselinux-devel zlib-devel
yum install wget -y
wget https://www.openssl.org/source/old/1.1.1/openssl-1.1.1v.tar.gz --no-check-certificate
wget https://mirrors.aliyun.com/pub/OpenBSD/OpenSSH/portable/openssh-9.6p1.tar.gz
mv openssh-9.6p1。tar.gz /root/upload-ssh
mv openssl-1.1.1v。tar.gz /root/upload-ssh
tar -czvf upload-ssh.tar.gz -C /root upload-ssh
```
## 将upload-ssh.tar.gz放置到目标主机的相关文件夹下，解压安装
``` shell
tar zxvf upload-ssh.tar.gz
cd upload-ssh
rpm -Uvh --force --nodeps *.rpm
cd ..
```

### 解压 openssl 与 openssh
``` shell
cd upload-ssh
tar zxvf openssl-1.1.1v。tar.gz
tar zxvf openssh-9.6p1。tar.gz
```
### 安装openssl
``` shell
cd openssl-1.1.1v
./config --prefix=/usr/local/openssl
make && make install
# 删除自带的ssl，有可能会把nginx一起带走，可不做
# yum remove openssl
# 查看版本，确定是否安装成功
/usr/local/openssl/bin/openssl version
# 如果报错
ldd /usr/local/openssl/bin/openssl
# 可能会存在缺少两个库文件 libssl.so.1.1和libcrypto.so.1.1
ln -s /usr/local/openssl/lib/libssl.so.1.1 /usr/lib64/libssl.so.1.1
ln -s /usr/local/openssl/lib/libcrypto.so.1.1 /usr/lib64/libcrypto.so.1.1
# 再次查看
ldd /usr/local/openssl/bin/openssl
/usr/local/openssl/bin/openssl version
# 软链接
ln -s /usr/local/openssl/bin/openssl /usr/bin/
openssl version
# 输出为 OpenSSL 1.1.1v  1 Aug 2023 方为正确
cd ..
```

### 安装openssh
``` shell
cd openssh-9.6p1
./configure --prefix=/usr/local/openssh --with-zlib=/usr/local/zlib --with-ssl-dir=/usr/local/openssl/
make && make install
# 卸载原有ssh
yum remove openssh
# 修改配置文件
echo "PasswordAuthentication yes" >> /usr/local/openssh/etc/sshd_config
echo "PermitRootLogin yes" >> /usr/local/openssh/etc/sshd_config
echo "PubkeyAuthentication yes" >> /usr/local/openssh/etc/sshd_config
cp contrib/redhat/sshd.init /etc/init.d/sshd
chkconfig --add sshd
# 建立连接
cp -s /usr/local/openssh/etc/sshd_config /etc/ssh/sshd_config
ln -s /usr/local/openssh/sbin/sshd /usr/sbin/sshd
ln -s /usr/local/openssh/bin/ssh /usr/bin/ssh
ln -s /usr/local/openssh/bin/ssh-keygen /usr/bin/ssh-keygen
rm /etc/ssh/ssh_host_ecdsa_key.pub
ln -s /usr/local/openssh/etc/ssh_host_ecdsa_key.pub /etc/ssh/ssh_host_ecdsa_key.pub
# 启动
systemctl start sshd.service
# 查看启动情况
systemctl status sshd.service
# 开机自启
systemctl enable sshd.service
# 查看版本
ssh -V
# 输出为 OpenSSH_9.6p1, OpenSSL 1.1.1v  1 Aug 2023 方为正确
```
