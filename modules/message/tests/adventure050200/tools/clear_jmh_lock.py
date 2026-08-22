import os
import tempfile

temp_dir = tempfile.gettempdir()
lock_path = os.path.join(temp_dir, "jmh.lock")
print("temp_dir=" + temp_dir)
print("lock_path=" + lock_path)
print("exists=" + str(os.path.exists(lock_path)))
if os.path.exists(lock_path):
    os.remove(lock_path)
    print("removed=true")
else:
    print("removed=false")
