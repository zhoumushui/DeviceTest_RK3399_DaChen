::==================�������ȡ����IP(������)��MAC��ַ==============
::code by youxi01@bbs.bathome.net 2008-1-5
@echo off
setlocal enabledelayedexpansion
title ��ȡ����IP(������)��MAC��ַ
Rem '/*=========��ʼ������=============*/
set "Space=        "
set "PH_addr=%Space%Physical Address"  %'/*-----�����ַ-------*/%
set "IP_addr=%Space%IP Address" %'/*------IP��ַ(������)--------*/%
Rem ===========������===================
for /f "tokens=1,* delims=." %%i in ('ipconfig /all') do (
   for %%a in (PH_addr IP_addr) do (
      if "%%i"=="!%%a!" set %%a=%%j
   )
)
Rem '/*===========�Խ�����д���===========
set PH_addr=%PH_addr:*:=%
set IP_addr=%IP_addr:*:=%
Rem '/*===========������===============
echo.
echo IP��ַΪ��%IP_addr%
echo.
echo ���������ַΪ��%PH_addr%

pause>nul