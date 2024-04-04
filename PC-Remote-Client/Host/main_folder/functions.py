import os
import json
from flask import request as req
import json
from fuzzywuzzy import process
import pyautogui
from ctypes import cast, POINTER
from comtypes import CLSCTX_ALL
from pycaw.pycaw import AudioUtilities, IAudioEndpointVolume
import pygetwindow as gw
import time
from pathlib import Path
import os

import comtypes
from flask import request as req

from classes import er





def createLink(name, url): 
    try:
        shortcut_path = Path(__file__).resolve().parent/ f'{name}.url'
        with open(shortcut_path, 'w') as shortcut:
            shortcut.write('[InternetShortcut]\n')
            shortcut.write(f'URL={url}')
            return er.r0
    except Exception as e:
        return er.r3

def checkLink(name): 
    try:
        shortcut_path = Path(__file__).resolve().parent/ f'{name}.url'
        if os.path.exists(shortcut_path):
            was_found = True
        else:
            was_found = False
    except Exception as e:
        return er.r3
    return was_found

def check_and_create_json_file(default_name):
    filename = "devicename.json"
    if not os.path.exists(filename):
        with open(filename, 'w') as f:
            json.dump({"device_name": default_name}, f)

def onStartup():
    main_folder = "main_folder"
    folders_to_check = ["for_uploads", "for_downloads", "for_screenshots"]
    
    
    if not os.path.exists(main_folder):
        os.makedirs(main_folder)

    
    for folder in folders_to_check:
        folder_path = os.path.join(main_folder, folder)
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
    
    
    check_and_create_json_file("default_name")
    
    
    links_to_check = {"Youtube": "https://www.youtube.com/",
                      "Disney": "https://www.disneyplus.com/",
                      "Netflix": "https://www.netflix.com/"}
    
    for name, url in links_to_check.items():
        was_found = checkLink(name)
        if not was_found:
            createLink(name, url)

def save_json_file(file_path, data):
    with open(file_path, "w") as file:
        json.dump(data, file)


def find_closest_match(input_string, search_list):
    closest_match, _ = process.extractOne(input_string,search_list)
    return closest_match if _ > 70 else None

def fullscreen(screenName, timeout=30, polling_interval=1,):
    start_time = time.time()
    while time.time() - start_time < timeout:
        try:
            fenster_titel_liste = gw.getAllTitles()
            window_name = find_closest_match(screenName, fenster_titel_liste)
            if window_name:
                gw.getWindowsWithTitle(window_name)[0].activate()
                gw.getWindowsWithTitle(window_name)[0].maximize()
                return er.r0
        except Exception as e:
            return er.r3
        time.sleep(polling_interval)
    return er.r4

def is_request_type(RequestType):
    def decorator(route_function):
        def wrapper(*args, **kwargs):
            comtypes.CoInitialize()
            if req.method == RequestType:
                return route_function(*args, **kwargs)
            else:
                return er.r1
        wrapper.__name__ = route_function.__name__ + '_wrapper'
        return wrapper
    return decorator


def load_json_file(file_path):
    with open(file_path, "r", encoding="utf-8") as file:
        data = json.load(file)
    return data



def openProgramm(programmName):
    try:
        os.startfile(programmName)
        return er.r0
    except Exception as e:
        return er.r3






def keyStroke(key):
    try:
        for char in key:
            if char == '@':
                pyautogui.hotkey('ctrl', 'alt', 'q')
            else:
                pyautogui.write(char)
        return True
    except Exception as e:
        return False

def set_volume(increment):
    devices = AudioUtilities.GetSpeakers()
    interface = devices.Activate(
        IAudioEndpointVolume._iid_, CLSCTX_ALL, None)
    volume_interface = cast(interface, POINTER(IAudioEndpointVolume))
    current_volume = volume_interface.GetMasterVolumeLevelScalar()
    new_volume = min(1.0, max(0.0, current_volume + increment))
    volume_interface.SetMasterVolumeLevelScalar(new_volume, None)

import pyautogui

def move_cursor_relative(x_offset, y_offset):
    current_position = pyautogui.position()
    screen_width, screen_height = pyautogui.size()
    border_margin = 20
    new_x = min(max(border_margin, current_position[0] + x_offset), screen_width - border_margin)
    new_y = min(max(border_margin, current_position[1] + y_offset), screen_height - border_margin)
    pyautogui.moveTo(new_x, new_y)

def mouseClick(key, amount):
    try:
        pyautogui.click(button = key, clicks = amount)
        return er.r0
    except Exception as e:
        return er.r5

def mouseScroll(amount):
    pyautogui.scroll(amount)




