public class MyEventHandler extends EventHandler{

    //用户接收从客户端读取到的字节数组
    public Reply onRead(byte[] readBytes){
        System.out.println("server has read: "+new String(readBytes));
        return new Reply(false,null);
    }

}
