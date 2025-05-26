def range_limit(vol):
    if (vol.value < 0.0):
        vol.value = 0.0
    if (vol.value > 1.0):
        vol.value = 1.0

def volume_change(vol_mic, vol_media):
    while  True:
        try:
            volume = float(input("Enter increasing volume: "))
            
            vol_mic.value += volume
            range_limit(vol_mic)
            
            vol_media.value -= volume
            range_limit(vol_media)

        except KeyboardInterrupt:
            break


        
