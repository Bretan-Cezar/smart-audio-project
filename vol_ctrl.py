def volume_change(vol_mic, vol_media, lock):
    while  True:
        try:
            volume = input("Enter increasing volume: ")

            lock.acquire()
            vol_mic += volume
            vol_media -= vol_mic
            lock.release()
        except KeyboardInterrupt:
            break
