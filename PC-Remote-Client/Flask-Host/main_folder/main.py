import json
from flask import Flask, request as req,  send_file
import pyautogui
import os
from PIL import ImageGrab 
import time 
import datetime
import threading

from classes import net, er, spot, dis,you

from functions import set_volume,move_cursor_relative
from functions import mouseClick, keyStroke, fullscreen, is_request_type
from functions import load_json_file, openProgramm, onStartup, mouseScroll
from gui import create_gui, update_terminal
import time


app = Flask(__name__)
UPLOAD_FOLDER = 'for_uploads/'
SCREENSHOT_FOLDER = "for_screenshots/"
DOWNLOAD_FOLDER = 'for_downloads/'

last_mouse_move_time = None
update_terminal_lock = threading.Lock()




@app.route("/scroll", methods=['POST']) 
@is_request_type("POST")                 
def Scroll():
    data = req.get_data(as_text=True)
    mouseScroll(int(data))
    update_terminal("scrolled mouse")
    return "200"


@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in req.files:
        return 'No file part', 400
    file = req.files['file']
    if file.filename == '':
        return 'No selected file', 400
    if file:
        filename = "data_folder//" +  UPLOAD_FOLDER + "/" + file.filename
        os.makedirs(os.path.dirname(filename), exist_ok=True)  
        file.save(filename)
        update_terminal("file uploaded")
        return 'File uploaded successfully', 200

@app.route('/download', methods=['POST'])
def download_file():
    data = req.get_json()
    filename = data.get('filename')
    print(data)
    if not filename:
        return 'No filename provided', 400
    
    
    file_path = os.path.join(os.path.dirname(__file__), DOWNLOAD_FOLDER, filename)
    print(file_path)
    
    if not os.path.exists(file_path):
        print("File not found")
        return 'File not found', 404
    update_terminal("file downloaded")
    return send_file(file_path, as_attachment=True)


@app.route("/screenshot")
def screenshot():
    screenshot = ImageGrab.grab()
    current_time = datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") 
    save_path = os.path.join("data_folder//", SCREENSHOT_FOLDER, f"{current_time}.png")
    screenshot.save(save_path, "PNG")  
    update_terminal("took a screenshot")
    return "200"




@app.route("/downloadfolder")
def downloadfolder():
    folder_path = os.path.join(os.path.dirname(__file__), "data_folder//" + DOWNLOAD_FOLDER )
    download_files = os.listdir(folder_path)
    print(download_files)
    print(folder_path)
    return json.dumps(download_files)  
    
        


@app.route("/sendKeys", methods=['POST'])
@is_request_type("POST")
def sendKeys():
    data = req.get_data(as_text=True)
    keyStroke(data)
    update_terminal("sent some key input")
    return "200"




@app.route("/spo") 
def spotify():
    openProgramm(spot.location)
    fullscreen("Spotify")
    update_terminal("opened spotify")
    return "200"

@app.route("/dis") 
def disney():
    openProgramm(dis.location)
    update_terminal("opened disney")
    return "200"


@app.route("/net")
def netflix():
    openProgramm(net.location)
    fullscreen("Netflix")
    update_terminal("opened netflix")
    return "200"

@app.route("/you")
def youtube():
    openProgramm(you.location)
    update_terminal("youtube opened")
    return "200"

@app.route("/name")
def name():
    devicename = load_json_file("devicename.json")
    return devicename["device_name"]

@app.route("/password", methods=['POST']) 
@is_request_type("POST")                      
def password():
    if os.path.exists("password.json"):
        password_data = load_json_file("password.json")
        data = req.get_data(as_text=True)
        if data == password_data.get("password"):
            update_terminal("connected to device")
            return "200"
        else:
            return "400"
    else:
        update_terminal("connected to device")
        return "200"



@app.route("/fuScr") 
def fullscreenpress():
    try:
        pyautogui.hotkey("F")
        update_terminal("entered fullscreen")
        return er.r0
    except:
        return er.r3

@app.route("/left") 
def back():
    try:
        pyautogui.hotkey("left")
        update_terminal("sent left key")
        return er.r0
    except:
        return er.r0

@app.route("/right") 
def forward():
    try:
        pyautogui.hotkey("right")
        update_terminal("sent right key")
        return er.r0
    except:
        return er.r0
    
@app.route("/backspace") 
def delete():
    try:
        pyautogui.hotkey("backspace")
        update_terminal("sent backspace key")
        return er.r0
    except:
        return er.r0

@app.route("/enter") 
def enter():
    try:
        pyautogui.hotkey("enter")
        update_terminal("sent enter key")
        return er.r0
    except:
        return er.r0

@app.route("/clP") 
def closeProgramm():
    try:
        pyautogui.hotkey("alt","f4")
        update_terminal("closed program")
        return er.r0
    except:
        return er.r3


def update_terminal_after_delay():
    global last_mouse_move_time
    with update_terminal_lock:
        current_time = time.time()
        if last_mouse_move_time is not None and current_time - last_mouse_move_time >= 1:
            update_terminal("moved mouse")
            last_mouse_move_time = None



def schedule_terminal_update():
    threading.Timer(1, update_terminal_after_delay).start()

@app.route("/mouseMove", methods=['POST'])
@is_request_type("POST")
def mouseMove():
    global last_mouse_move_time
    data = req.get_data(as_text=True)
    x, y = map(int, data.split("/"))
    
    try:
        move_cursor_relative(x, y)
    except Exception as e:
        print(f"Error: {e}")
        return er.r5
    
    last_mouse_move_time = time.time()
    schedule_terminal_update()
    
    return er.r0


@app.route("/lc")
def leftClick():
 mouseClick("left" ,1  )
 update_terminal("sent left click")
 return "200"

@app.route("/rc") 
def rightClick():
    mouseClick("right",1)
    update_terminal("sent right click")
    return "200"

@app.route("/space") 
def spaceBar():
    keyStroke(" ")
    update_terminal("sent space bar")
    return "200"

@app.route("/sound",methods=['POST']) 
@is_request_type("POST")                          
def changeSound():
    volume = req.get_data(as_text=True)
    set_volume(float(volume))
    update_terminal("adjusted sound")
    return "200"

if __name__ == '__main__':
    
    onStartup()
    create_gui(app)
