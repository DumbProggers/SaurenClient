package com.sauren.sauren;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.PointerByReference;

import static com.sauren.sauren.EnumerateWindows.Kernel32.*;

public class EnumerateWindows {
    private static final int MAX_TITLE_LENGTH = 1024;
    public static String activeWindow(){
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        PointerByReference pointer = new PointerByReference();
        User32DLL.GetWindowThreadProcessId(User32DLL.GetForegroundWindow(), pointer);
        Pointer process = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, false, pointer.getValue());
        Psapi.GetModuleBaseNameW(process, null, buffer, MAX_TITLE_LENGTH);

        if(Native.toString(buffer).length()==0){
            return "UNKNOW";
        }else {
            int index = Native.toString(buffer).toLowerCase().indexOf(".exe");
            if(Native.toString(buffer).substring(0,index).toLowerCase().equals("explorer")){
                return "Explorer";
            }
            System.out.println(Native.toString(buffer).substring(0,index));
            return Native.toString(buffer).substring(0,index);
        }
    }
    public static String activeTitleWindow(){
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        User32DLL.GetWindowTextW(User32DLL.GetForegroundWindow(), buffer, MAX_TITLE_LENGTH);
        String activeTitle = Native.toString(buffer).replace(" ","_");
        //регулярка на языке дауна)

        activeTitle =activeTitle.replace("-",":");
        activeTitle =activeTitle.replace("(","");
        activeTitle =activeTitle.replace(")","");
        activeTitle =activeTitle.replace(":","");
        activeTitle =activeTitle.replace("?","");
        activeTitle =activeTitle.replace("\\","");
        activeTitle =activeTitle.replace("/","");
        activeTitle =activeTitle.replace(">","");
        activeTitle =activeTitle.replace("<","");
        activeTitle =activeTitle.replace("*","");
        activeTitle =activeTitle.replace("\"","");
        activeTitle =activeTitle.replace("\"","");
        activeTitle =activeTitle.replace(".","");
        activeTitle =activeTitle.replace(",","");
        activeTitle =activeTitle.replace("|","");


        if(activeTitle.length()==0){
            return "Desktop";
        }else {
            System.out.println(activeTitle);
            return activeTitle;
        }
    }
    static class Psapi {
        static { Native.register("psapi"); }
        public static native int GetModuleBaseNameW(Pointer hProcess, Pointer hmodule, char[] lpBaseName, int size);
    }
    static class Kernel32 {
        static { Native.register("C:\\Windows\\System32\\kernel32"); }
        public static int PROCESS_QUERY_INFORMATION = 0x0400;
        public static int PROCESS_VM_READ = 0x0010;
        public static native int GetLastError();
        public static native Pointer OpenProcess(int dwDesiredAccess, boolean bInheritHandle, Pointer pointer);
    }
    static class User32DLL {
        static { Native.register("user32"); }
        public static native int GetWindowThreadProcessId(HWND hWnd, PointerByReference pref);
        public static native HWND GetForegroundWindow();
        public static native int GetWindowTextW(HWND hWnd, char[] lpString, int nMaxCount);
    }
}