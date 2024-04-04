import os



class Errors:
    def __init__(self, r0, r1, r2, r3, r4, r5 ):
        self.r0 = r0
        self.r1 = r1
        self.r2 = r2
        self.r3 = r3
        self.r4 = r4
        self.r5 = r5

class Spotify:
    def __init__(self, location):
        self.location = location
        


class Netflix:
    def __init__(self, location,altloc):
        self.location = location
        self.altloc = altloc
    def open(self):
        try:
            os.startfile(self.location)
            return " opened successfully"
        except Exception as e:
            return f"Error opening : {e}"    

class Chrome:
    def __init__(self, location):
        self.location = location
    def open(self):
        try:
            os.startfile(self.location)
            return " opened successfully"
        except Exception as e:
            return f"Error opening : {e}"     


class Disney:
    def __init__(self, location):
        self.location = location
    def open(self):
        try:
            os.startfile(self.location)
            return " opened successfully"
        except Exception as e:
            return f"Error opening : {e}"     
     

class Youtube:
    def __init__(self, location):
        self.location = location
    def open(self):
        try:
            os.startfile(self.location)
            return " opened successfully"
        except Exception as e:
            return f"Error opening : {e}"     
    
                








er = Errors(r0="Errorcode 000" ,r1="Errorcode 001", r2="Errorcode 002", r3="Errorcode 003", r4="Errorcode 004", r5="Errorcode 005")

net = Netflix(location=os.path.join(os.path.expanduser('~'), 'Desktop') + "\\Netflix.lnk", altloc ="Netflix.url")

spot = Spotify(location=os.path.join(os.path.expanduser('~'), 'Desktop') + "\\Spotify.lnk")

dis = Disney (location= "Disney.url")

you = Youtube(location="Youtube.url")



