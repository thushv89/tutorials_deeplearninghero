import requests
import pathlib
import base64
import json
import time
from multiprocessing import Pool

base_url = "https://ml-image-tagger-2.uc.r.appspot.com"

images_dir = pathlib.Path("images")
upload_url = f"{base_url}/upload_image"
query_url = f"{base_url}/get_image_tags"

def upload_images(upload_url: str, images_dir: pathlib.Path) -> list[str]:
    uuids = []
    for p in images_dir.iterdir():
        with open(p, "rb") as f:
            encoded_string = base64.b64encode(f.read())
        res = requests.post(upload_url, data=encoded_string)
        uuids.append(json.loads(res.content.decode("utf-8"))["image_id"])
    return uuids

def query_images(query_url, uuid):

    res = requests.get(query_url+f"?uuid={uuid}")
    return {"uuid": uuid, "tags": json.loads(res.content.decode("utf-8"))}

if __name__=="__main__":

    uuids = upload_images(upload_url, images_dir)

    time.sleep(10)

    print("Warming up")
    res = query_images(query_url, uuids)
    print("Done")
    p = Pool(10)
    for _ in range(25):
        print(".", end="")
        res = p.starmap(query_images, list(zip([query_url for _ in uuids], uuids)))

    print(res)


