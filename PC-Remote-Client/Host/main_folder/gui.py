import signal
import tkinter as tk
import json
import os
import threading
from tkinter import ttk
import datetime 
import time
import qrcode
import socket
from PIL import ImageTk
import webbrowser

flask_host = None
MAX_TERMINAL_LINES = 15

def get_server_ip():
    if flask_host:  
        return flask_host
    else:
        return "Not available"

def open_github_profile(event):
    webbrowser.open_new("https://github.com/CopyPasteUser")

def update_terminal(message):
    global terminal_text
    if terminal_text is None:
        print("Terminal text widget is not initialized.")
        return
    ct = datetime.datetime.now()
    
    terminal_text.config(state=tk.NORMAL)  
    terminal_text.insert(tk.END,"[" + datetime.datetime.fromtimestamp(time.time()).strftime("%Y-%m-%d %H:%M:%S")  + "]: "+  message + "\n")
    terminal_text.see(tk.END)  
    if int(terminal_text.index('end-1c').split('.')[0]) > MAX_TERMINAL_LINES:
        terminal_text.delete('1.0', tk.END)
    terminal_text.config(state=tk.DISABLED)  


def open_folder(folder_name):
    folder_path = os.path.join(os.getcwd(), folder_name)
    os.startfile(folder_path)

def save_device_name(device_name):
    with open("devicename.json", "w") as json_file:
        json.dump({"device_name": device_name}, json_file)
        print("Device name saved to devicename.json")





import socket

def start_client(app):
    flask_thread = threading.Thread(target=app.run, kwargs={"host":"0.0.0.0","port":"5500"})
    flask_thread.start()
    flask_thread.join(timeout=1)  
    
    flask_host = get_host_ip()
    flask_port = app.config.get('SERVER_PORT', '5500')
    if flask_host and flask_port:
        update_terminal("Client started at http://{}:{}".format(flask_host, flask_port))
    else:
        update_terminal("Unable to retrieve Flask host and port from the app configuration.")

def get_host_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        host_ip = s.getsockname()[0]
        s.close()
        return host_ip
    except Exception as e:
        print("Error getting host IP:", str(e))
        return None





def stop_client():
    os.kill(os.getpid(), signal.SIGINT)

def create_gui(app):
    global terminal_text
    
    def generate_qr_code(ip_address):
        qr = qrcode.QRCode(version=1, error_correction=qrcode.constants.ERROR_CORRECT_L, box_size=10, border=4)
        qr.add_data(ip_address)
        qr.make(fit=True)
        img = qr.make_image(fill_color="black", back_color="white").convert("RGBA")

       
        datas = img.getdata()
        newData = []
        for item in datas:
            if item[0] != 255 and item[1] != 255 and item[2] != 255:
                newData.append(item)
            else: 
                newData.append((255, 255, 255, 0))
        img.putdata(newData)
        return img
    
    


    def save_password():
        password = password_entry.get()
        with open("password.json", "w") as json_file:
            json.dump({"password": password}, json_file)
        set_password_button.config(state=tk.NORMAL if password_protect_var.get() else tk.DISABLED)
        if not password_protect_var.get():
            remove_password()

    def clear_text_entry(entry):
        entry.delete(0, tk.END)

    def remove_password():
        if os.path.exists("password.json"):
            os.remove("password.json")

    def get_server_ip():
        try:
            hostname = socket.gethostname()
            ip_address = socket.gethostbyname(hostname)
            return ip_address
        except socket.error as e:
            print("Error getting server IP:", e)
            return None

    def check_password_file():
        try:
            with open("password.json", "r") as json_file:
                data = json.load(json_file)
                return bool(data.get("password"))
        except FileNotFoundError:
            return False

    root = tk.Tk()
    root.title("Remote PC Client")
    root.geometry("800x600")

    
    tab_control = ttk.Notebook(root)
    tab_control.pack(fill="both", expand=True)


    
    connection_frame = ttk.Frame(tab_control)
    tab_control.add(connection_frame, text="Connection")
    connection_frame.columnconfigure(0, weight=1)
    connection_frame.columnconfigure(1, weight=1)

    
    start_client_button = tk.Button(connection_frame, text="\U0001F4E8 Start Client", command=lambda: start_client(app), font=("Arial", 12))
    start_client_button.grid(row=0, column=0, padx=5, pady=5)

    
    stop_client_button = tk.Button(connection_frame, text="\U0001F6D1 Stop Client", command=stop_client, font=("Arial", 12))
    stop_client_button.grid(row=0, column=1, padx=5, pady=5)

    
    server_ip_label = tk.Label(connection_frame, text="Server IP: " + get_host_ip() , font=("Arial", 10))
    server_ip_label.grid(row=1, column=0, columnspan=2, padx=5, pady=5)

    
    ip_address = get_host_ip()
    if ip_address:
        qr_code_img = generate_qr_code(ip_address)
        qr_code_img_tk = ImageTk.PhotoImage(qr_code_img)
        qr_code_label = tk.Label(connection_frame, image=qr_code_img_tk)
        qr_code_label.image = qr_code_img_tk  
        qr_code_label.grid(row=2, column=0, columnspan=2, padx=5, pady=5)
    else:
        error_label = tk.Label(connection_frame, text="Error: Unable to retrieve server IP", font=("Arial", 10), fg="red")
        error_label.grid(row=2, column=0, columnspan=2, padx=5, pady=5)
    
    
    settings_frame = ttk.Frame(tab_control)
    tab_control.add(settings_frame, text="Settings")
    settings_frame.columnconfigure(0, weight=1)
    settings_frame.columnconfigure(1, weight=1)

    device_name_entry = tk.Entry(settings_frame)
    device_name_entry.grid(row=0, column=0, padx=5, pady=5)

    save_device_name_button = tk.Button(settings_frame, text="\U0001F4BE Save Device Name", command=lambda: [save_device_name(device_name_entry.get()), clear_text_entry(device_name_entry)], font=("Arial", 12))
    save_device_name_button.grid(row=0, column=1, padx=5, pady=5)

    
    password_protect_var = tk.BooleanVar()
    password_protect_checkbutton = ttk.Checkbutton(settings_frame, text="Password Protect", variable=password_protect_var, command=lambda: [password_entry.config(state=tk.NORMAL if password_protect_var.get() else tk.DISABLED),
                                                                                                                                                              set_password_button.config(state=tk.NORMAL if password_protect_var.get() else tk.DISABLED), remove_password() if not password_protect_var.get() else None])
    password_protect_checkbutton.grid(row=1, column=0, columnspan=2, padx=5, pady=5)
    password_protect_var.set(True) if check_password_file() else None

    
    password_entry = tk.Entry(settings_frame)
    password_entry.grid(row=2, column=0, padx=5, pady=5)
    password_entry.config(state=tk.NORMAL if password_protect_var.get() else tk.DISABLED)

    
    set_password_button = tk.Button(settings_frame, text="\U0001F511 Set Device Password", command=lambda: [save_password(), clear_text_entry(password_entry)], font=("Arial", 12))
    set_password_button.grid(row=2, column=1, padx=5, pady=5)
    set_password_button.config(state=tk.NORMAL if password_protect_var.get() else tk.DISABLED)

   
    files_frame = ttk.Frame(tab_control)
    tab_control.add(files_frame, text="Files")

   
    upload_button = tk.Button(files_frame, text="\U0001F4C2 Upload Folder", command=lambda: open_folder("main_folder//" +"for_uploads"), font=("Arial", 12))
    upload_button.grid(row=0, column=0, padx=5, pady=5)

    
    download_button = tk.Button(files_frame, text="\U0001F4C1 Download Folder", command=lambda: open_folder("main_folder//" +"for_downloads"), font=("Arial", 12))
    download_button.grid(row=0, column=1, padx=5, pady=5)
    
    
    download_button = tk.Button(files_frame, text="\U0001F4C1 Screenshot Folder", command=lambda: open_folder("main_folder//" +"for_screenshots"), font=("Arial", 12))
    download_button.grid(row=1, column=1, padx=5, pady=5)

    
    logs_frame = ttk.Frame(tab_control)
    tab_control.add(logs_frame, text="Logs")
    logs_frame.columnconfigure(0, weight=1)
    logs_frame.columnconfigure(1, weight=1)

    
    terminal_text = tk.Text(logs_frame, wrap=tk.WORD, state=tk.DISABLED) 
    terminal_text.grid(row=0, column=0, columnspan=2, padx=5, pady=5, sticky="nsew")
   
    about_frame = ttk.Frame(tab_control)
    tab_control.add(about_frame, text="About")

    
    app_info_label = tk.Label(about_frame, text="Remote PC Client v1.0", font=("Arial", 14, "bold"))
    app_info_label.grid(row=0, column=0, padx=10, pady=10, sticky="w")

    
    description_text = "This application allows you to remotely connect to your PC and perform various tasks."
    description_label = tk.Label(about_frame, text=description_text, font=("Arial", 12), wraplength=500, justify="left")
    description_label.grid(row=1, column=0, padx=10, pady=5, sticky="w")

   
    features_label = tk.Label(about_frame, text="Features:", font=("Arial", 12, "underline"))
    features_label.grid(row=2, column=0, padx=10, pady=5, sticky="w")

    features_list = [
    "Remote control of your PC",
    "File transfer between devices",
    "Password protection for security",
    "QR code generation for easy connection"
    ]

    for i, feature in enumerate(features_list, start=3):
        feature_label = tk.Label(about_frame, text="• " + feature, font=("Arial", 11), wraplength=500, justify="left")
        feature_label.grid(row=i, column=0, padx=10, pady=2, sticky="w")

    
   
    credits_label = tk.Label(about_frame, text="Developed by: CopyPasteUser", font=("Arial", 11), fg="blue", cursor="hand2")
    credits_label.grid(row=i+1, column=0, padx=10, pady=10, sticky="w")
    credits_label.bind("<Button-1>", open_github_profile)


    root.mainloop()  
