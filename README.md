# LinphoneTest
linphone封装后测试地址
# sipphone使用
> 使用sipphonp最主要是调用SipUtil类的方法来操作打电话功能,所以需要先创建SipUtil实例，通过静态方法getIns（）创建

---
> 使用打电话功能，需要先初始化，初始化成功后，然后注册，注册成功后方可接打电话

---
### SipUtil
1. 创建实例

```
public static synchronized SipUtils getIns（）
```
return  | 返回值释义
---|---
SipUtil | 返回实例

---
2. 初始化

```
public void init(final WeakReference<Context> reference,  CoreListenerStub coreListenerStub)
```
Parameters  |参数释义
---|---
reference | 全局上下文的弱引用，注：不要传activity
coreListenerStub|linphone提供的监听接口，通过不同方法可监听初始化，注册，通话的状态，后面详说
> 使用方法：SipUtils.getIns().init(new WeakReference<Context>(context), coreListener);
---
3. 注册

```
public void registerSip(String username, String password, String displayname, String domain)
```
Parameters  |参数释义
---|---
username | 提供的sipname
password | 提供的sippassword
displayname | 要显示的名字，注：就是打出去后对方手机上显示的名字
domain | 域名 注：以上参数一般是服务器给的
注：注册状态也是通过初始化时的接口监听
> 使用方法：SipUtils.getIns().registerSip(username,
                        password,
                        displayname,
                        domain);
---
4. 打电话

```
public void call(String number, Context context)
```
Parameters  |参数释义
---|---
number | 拨打的电话
context | 全局上下文对象

注：拨打状态也是通过初始化时的接口监听
> 使用方法：SipUtils.getIns().call("69801006278", context);

---

5. 挂电话

```
public void hangUp()
```


注：挂断状态也是通过初始化时的接口监听
> 使用方法：public void hangUp();
---
6. 接电话

```
public boolean answer(Context context)
```
Parameters  |参数释义
---|---
context | 全局上下文对象

return  | 返回值释义
---|---
boolean | 判断网络快慢（没用上）

注：接电话状态也是通过初始化时的接口监听
> 使用方法：SipUtils.getIns().answer(context);

---
7. 电话静音（该方法属于音频管理方法，不想用的话可以自己封装，需要动态获取RECORD_AUDIO权限）

```
public void switchingMute(boolean isMicMuted) {
```
Parameters  |参数释义
---|---
isMicMuted | 是否静音 true静音/false不静音

> 使用方法：SipUtils.getIns().switchingMute(isMicMuted);

---

8. 免提（该方法属于音频管理方法，不想用的话可以自己封装，需要动态获取RECORD_AUDIO权限）

```
public void switchingSpeakers(boolean isSpeakers)
```
Parameters  |参数释义
---|---
isSpeakers | 是否打开免提，true打开免提/false关掉免提

> 使用方法：SipUtils.getIns().switchingSpeakers(isSpeakerEnabled);

---
### CoreListenerStub 接口释义






