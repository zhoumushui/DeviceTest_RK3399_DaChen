::==================批处理获取本机IP(局域网)及MAC地址==============
::code by youxi01@bbs.bathome.net 2008-1-5
@echo off
setlocal enabledelayedexpansion
title 获取本机IP(局域网)及MAC地址
Rem '/*=========初始化设置=============*/
set "Space=        "
set "PH_addr=%Space%Physical Address"  %'/*-----物理地址-------*/%
set "IP_addr=%Space%IP Address" %'/*------IP地址(局域网)--------*/%
Rem ===========主程序===================
for /f "tokens=1,* delims=." %%i in ('ipconfig /all') do (
   for %%a in (PH_addr IP_addr) do (
      if "%%i"=="!%%a!" set %%a=%%j
   )
)
Rem '/*===========对结果进行处理===========
set PH_addr=%PH_addr:*:=%
set IP_addr=%IP_addr:*:=%
Rem '/*===========结果输出===============
echo.
echo IP地址为：%IP_addr%
echo.
echo 网卡物理地址为：%PH_addr%

pause>nul