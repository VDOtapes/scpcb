; ID: 2975
; Author: RifRaf, further modified by MonocleBios
; Date: 2012-09-11 11:44:22
; Title: Safe Loads (b3d) ;strict loads sounds more appropriate IMO
; Description: Get the missing filename reported

;safe loads for mav trapping media issues




;basic wrapper functions that check to make sure that the file exists before attempting to load it, raises an RTE if it doesn't
;more informative alternative to MAVs outside of debug mode, makes it immiediately obvious whether or not someone is loading resources
;likely to cause more crashes than 'clean' CB, as this prevents anyone from loading any assets that don't exist, regardless if they are ever used
;added zero checks since blitz load functions return zero sometimes even if the filetype exists
Function LoadImage_Strict(file$)
	If FileType(file$)<>1 Then RuntimeError "Image " + file$ + " missing. "
	tmp = LoadImage(file$)
	
	;attempt to load the image again
	If tmp = 0 Then LoadImage(file)
	
	If tmp = 0 Then
		;if loading failed again, add an error message to the console and return a black image
		CreateConsoleMsg("Loading image ''"+file+"'' failed")
		If ConsoleOpening Then
			ConsoleOpen = True
		EndIf
		
		Return MenuBlack
	EndIf
	
	Return tmp
End Function

;Function LoadSound_Strict(file$)
;	
;	If FileType(file$)<>1 Then 
;		CreateConsoleMsg("Sound " + file$ + " not found.")
;		ConsoleInput = ""
;		ConsoleOpen = True
;		Return 0
;	EndIf
;   tmp = LoadSound(file$)
;	If tmp = 0 Then 
;		CreateConsoleMsg("Failed to load Sound:" + file$)
;		ConsoleInput = ""
;		ConsoleOpen = True
;	EndIf
;	Return tmp
;End Function

Type Sound
	Field internalHandle%
	Field name$
	Field channels%[32]
	Field releaseTime%
End Type

Function AutoReleaseSounds()
	Local snd.Sound
	For snd.Sound = Each Sound
		Local tryRelease% = True
		For i=0 To 31
			If snd\channels[i]<>0 Then
				If ChannelPlaying(snd\channels[i]) Then
					tryRelease = False
					snd\releaseTime = MilliSecs()+5000
					Exit
				EndIf
			EndIf
		Next
		If tryRelease Then
			If snd\releaseTime<MilliSecs() Then
				If snd\internalHandle<>0 Then
					FreeSound snd\internalHandle
					snd\internalHandle = 0
				EndIf
			EndIf
		EndIf
	Next
End Function

Function PlaySound_Strict%(sndHandle%)
	Local snd.Sound = Object.Sound(sndHandle)
	If snd<>Null Then
		Local shouldPlay% = True
		For i=0 To 31
			If snd\channels[i]<>0 Then
				If Not ChannelPlaying(snd\channels[i]) Then
					If snd\internalHandle=0 Then
						If FileType(snd\name)<>1 Then
							CreateConsoleMsg("Sound "+snd\name+" not found.")
							If ConsoleOpening
								ConsoleOpen = True
							EndIf
						Else
							If EnableSFXRelease Then snd\internalHandle = LoadSound(snd\name)
						EndIf
						If snd\internalHandle = 0 Then
							CreateConsoleMsg("Failed to load Sound: "+snd\name)
							If ConsoleOpening
								ConsoleOpen = True
							EndIf
						EndIf
					EndIf
					snd\channels[i]=PlaySound(snd\internalHandle)
					ChannelVolume snd\channels[i],SFXVolume#
					snd\releaseTime = MilliSecs()+5000 ;release after 5 seconds
					Return snd\channels[i]
				EndIf
			Else
				If snd\internalHandle=0 Then
					If FileType(snd\name)<>1 Then
						CreateConsoleMsg("Sound "+snd\name+" not found.")
						If ConsoleOpening
							ConsoleOpen = True
						EndIf
					Else
						If EnableSFXRelease Then snd\internalHandle = LoadSound(snd\name)
					EndIf
						
					If snd\internalHandle = 0 Then
						CreateConsoleMsg("Failed to load Sound: "+snd\name)
						If ConsoleOpening
							ConsoleOpen = True
						EndIf
					EndIf
				EndIf
				snd\channels[i]=PlaySound(snd\internalHandle)
				ChannelVolume snd\channels[i],SFXVolume#
				snd\releaseTime = MilliSecs()+5000 ;release after 5 seconds
				Return snd\channels[i]
			EndIf
		Next
	EndIf
	
	Return 0
End Function

Function LoadSound_Strict(file$)
	Local snd.Sound = New Sound
	snd\name = file
	snd\internalHandle = 0
	snd\releaseTime = 0
	If (Not EnableSFXRelease) Then snd\internalHandle = LoadSound(snd\name)
	
	Return Handle(snd)
End Function

Function FreeSound_Strict(sndHandle%)
	Local snd.Sound = Object.Sound(sndHandle)
	If snd<>Null Then
		If snd\internalHandle<>0 Then
			FreeSound snd\internalHandle
			snd\internalHandle = 0
		EndIf
		Delete snd
	EndIf
End Function

Function LoadMesh_Strict(File$,parent=0)
	If FileType(File$)<>1 Then RuntimeError "3D Mesh " + File$ + " not found."
	tmp = LoadMesh(File$, parent)
	If tmp = 0 Then RuntimeError "Failed to load 3D Mesh: " + File$ 
	Return tmp  
End Function   

Function LoadAnimMesh_Strict(File$,parent=0)
	DebugLog File
	If FileType(File$)<>1 Then RuntimeError "3D Animated Mesh " + File$ + " not found."
	tmp = LoadAnimMesh(File$, parent)
	If tmp = 0 Then RuntimeError "Failed to load 3D Animated Mesh: " + File$ 
	Return tmp
End Function   

;don't use in LoadRMesh, as Reg does this manually there. If you wanna fuck around with the logic in that function, be my guest 
Function LoadTexture_Strict(File$,flags=1)
	If FileType(File$)<>1 Then RuntimeError "Texture " + File$ + " not found."
	tmp = LoadTexture(File$, flags)
	If tmp = 0 Then RuntimeError "Failed to load Texture: " + File$ 
	Return tmp 
End Function   

Function LoadBrush_Strict(file$,flags,u#=1.0,v#=1.0)
	If FileType(file$)<>1 Then RuntimeError "Brush Texture " + file$ + "not found."
	tmp = LoadBrush(file$, flags, u, v)
	If tmp = 0 Then RuntimeError "Failed to load Brush: " + file$ 
	Return tmp 
End Function 

Function LoadFont_Strict(file$="Tahoma", height=13, bold=0, italic=0, underline=0)
	If FileType(file$)<>1 Then RuntimeError "Font " + file$ + " not found."
	tmp = LoadFont(file, height, bold, italic, underline)  
	If tmp = 0 Then RuntimeError "Failed to load Font: " + file$ 
	Return tmp
End Function
;~IDEal Editor Parameters:
;~F#F#27#2E#46#7F#89#94#9B#A4#AB#B3
;~C#Blitz3D