using SqlSugar;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using WxFile.Entities;
using WxFile.Utils;

namespace WxFile.Controllers
{
    public class FileController : Controller
    {
        // GET: file
        public ActionResult Index()
        {
            FileUploadManager fileUploadManager = new FileUploadManager();
            SimpleClient<fileupload> FileDb = new SimpleClient<fileupload>(fileUploadManager.Db);
            return View(FileDb.GetList());
        }


        public ActionResult showfiles()
        {
            return View();
        }

        public ActionResult getfiles(string id)
        {
            FileUploadManager fileUploadManager = new FileUploadManager();
            SimpleClient<fileupload> FileDb = new SimpleClient<fileupload>(fileUploadManager.Db);
            if (id == null)
            {
                List<fileupload> list = FileDb.GetList();
                return Json(new { code = "0", msg = "", count = list.Count, data = list }, JsonRequestBehavior.AllowGet);
            }
            else
            {
                List<fileupload> list = fileUploadManager.Db.Ado.SqlQuery<fileupload>("select * from fileupload where id like @id", new SugarParameter("@id", id + ""));
                return Json(new { code = "0", msg = "", count = list.Count, data = list }, JsonRequestBehavior.AllowGet);
            }
        }

        [HttpPost]
        public ActionResult upload()
        {
            try
            {
                HttpFileCollectionBase files = Request.Files;
                HttpPostedFileBase file = files[0];
                //获取文件名后缀
                string extName = Path.GetExtension(file.FileName).ToLower();
                //获取保存目录的物理路径
                if (System.IO.Directory.Exists(Server.MapPath("/Content/images/")) == false)//如果不存在就创建images文件夹
                {
                    System.IO.Directory.CreateDirectory(Server.MapPath("/Content/images/"));
                }
                string path = Server.MapPath("/Content/images/"); //path为某个文件夹的绝对路径，不要直接保存到数据库
                                                          //    string path = "F:\\TgeoSmart\\Image\\";
                                                          //生成新文件的名称，guid保证某一时刻内图片名唯一（文件不会被覆盖）
                string fileNewName = Guid.NewGuid().ToString();
                string ImageUrl = path + fileNewName + extName;
                //SaveAs将文件保存到指定文件夹中
                file.SaveAs(ImageUrl);
                //此路径为相对路径，只有把相对路径保存到数据库中图片才能正确显示（不加~为相对路径）
                string url = "\\Content\\images\\" + fileNewName + extName;
                return Json(new
                {
                    Result = true,
                    Data = url
                });
            }
            catch (Exception exception)
            {
                return Json(new
                {
                    Result = false,
                    exception.Message
                });
            }
           
        }
        public ActionResult Save(int id,string title, string remark, string url)
        {
            FileUploadManager fileUploadManager = new FileUploadManager();
            SimpleClient<fileupload> FileDb = new SimpleClient<fileupload>(fileUploadManager.Db);
            try
            {
                var file=new fileupload()
                {
                    id = id,
                    filename = title,
                    filecontent = url,
                    filetype = remark,
                };
                FileDb.Insert(file);
                return Json(new
                {
                    Result = true
                });

            }
            catch (Exception exception)
            {
                return Json(new
                {
                    Result = true,
                    exception.Message
                });
            }
        }
    }
}