import React, { useState } from 'react';
import { AlertCircle, FileText, Plus, Trash2, Upload, Download } from 'lucide-react';

const API_URL = 'http://localhost:8080/api';

function App() {
  const [currentPage, setCurrentPage] = useState('login');
  const [token, setToken] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [loginData, setLoginData] = useState({ username: '', password: '' });

  const [invoiceData, setInvoiceData] = useState({
    title: '',
    invoiceDate: new Date().toISOString().split('T')[0],
    logo: null,
    items: [{ itemName: '', quantity: 1, unitPrice: 0 }]
  });

  const handleLogin = async () => {
    setError('');

    if (!loginData.username || !loginData.password) {
      setError('لطفاً نام کاربری و رمز عبور را وارد کنید');
      return;
    }

    try {
      const response = await fetch(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginData)
      });

      if (response.ok) {
        const data = await response.json();
        setToken(data.token);
        setCurrentPage('invoice');
        setSuccess('ورود موفقیت‌آمیز بود!');
      } else {
        setError('نام کاربری یا رمز عبور اشتباه است');
      }
    } catch (err) {
      setError('خطا در اتصال به سرور. مطمئن شوید Backend در حال اجراست.');
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleLogin();
    }
  };

  const handleLogoUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        setError('حجم فایل نباید بیشتر از 5 مگابایت باشد');
        return;
      }
      setInvoiceData({ ...invoiceData, logo: file });
      setError('');
    }
  };

  const addItem = () => {
    setInvoiceData({
      ...invoiceData,
      items: [...invoiceData.items, { itemName: '', quantity: 1, unitPrice: 0 }]
    });
  };

  const removeItem = (index) => {
    const newItems = invoiceData.items.filter((_, i) => i !== index);
    setInvoiceData({ ...invoiceData, items: newItems });
  };

  const updateItem = (index, field, value) => {
    const newItems = [...invoiceData.items];
    newItems[index][field] = field === 'itemName' ? value : parseFloat(value) || 0;
    setInvoiceData({ ...invoiceData, items: newItems });
  };

  const calculateTotal = () => {
    return invoiceData.items.reduce((sum, item) =>
        sum + (item.quantity * item.unitPrice), 0
    ).toLocaleString('fa-IR');
  };

  const handleSubmitInvoice = async () => {
    setError('');
    setSuccess('');

    // Validation
    if (!invoiceData.title.trim()) {
      setError('لطفاً عنوان فاکتور را وارد کنید');
      return;
    }

    const hasEmptyItems = invoiceData.items.some(item => !item.itemName.trim());
    if (hasEmptyItems) {
      setError('لطفاً نام همه اقلام را وارد کنید');
      return;
    }

    const formData = new FormData();

    const invoiceDto = {
      title: invoiceData.title,
      invoiceDate: invoiceData.invoiceDate,
      items: invoiceData.items.map(item => ({
        itemName: item.itemName,
        quantity: item.quantity,
        unitPrice: item.unitPrice
      }))
    };

    formData.append('invoice', new Blob([JSON.stringify(invoiceDto)], {
      type: 'application/json'
    }));

    if (invoiceData.logo) {
      formData.append('logo', invoiceData.logo);
    }

    try {
      const response = await fetch(`${API_URL}/invoices`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      });

      if (response.ok) {
        const invoice = await response.json();
        setSuccess('فاکتور با موفقیت ایجاد شد! در حال دانلود PDF...');

        // دانلود PDF
        setTimeout(async () => {
          try {
            const pdfResponse = await fetch(`${API_URL}/invoices/${invoice.id}/pdf`, {
              headers: { 'Authorization': `Bearer ${token}` }
            });

            if (pdfResponse.ok) {
              const blob = await pdfResponse.blob();
              const url = window.URL.createObjectURL(blob);
              const a = document.createElement('a');
              a.href = url;
              a.download = `invoice_${invoice.id}.pdf`;
              document.body.appendChild(a);
              a.click();
              document.body.removeChild(a);
              window.URL.revokeObjectURL(url);

              // Reset form
              setInvoiceData({
                title: '',
                invoiceDate: new Date().toISOString().split('T')[0],
                logo: null,
                items: [{ itemName: '', quantity: 1, unitPrice: 0 }]
              });
            } else {
              setError('خطا در دانلود PDF');
            }
          } catch (err) {
            setError('خطا در دانلود PDF');
          }
        }, 500);
      } else {
        const errorText = await response.text();
        setError(`خطا در ایجاد فاکتور: ${errorText}`);
      }
    } catch (err) {
      setError('خطا در اتصال به سرور');
    }
  };

  if (currentPage === 'login') {
    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md">
            <div className="text-center mb-8">
              <FileText className="w-16 h-16 mx-auto text-indigo-600 mb-4" />
              <h1 className="text-3xl font-bold text-gray-800">سیستم فاکتور ساز</h1>
              <p className="text-gray-600 mt-2">لطفاً وارد حساب کاربری خود شوید</p>
            </div>

            {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 flex items-center gap-2">
                  <AlertCircle className="w-5 h-5 flex-shrink-0" />
                  <span className="text-sm">{error}</span>
                </div>
            )}

            <div className="space-y-4">
              <div>
                <label className="block text-gray-700 mb-2 font-medium text-right">نام کاربری</label>
                <input
                    type="text"
                    value={loginData.username}
                    onChange={(e) => setLoginData({ ...loginData, username: e.target.value })}
                    onKeyPress={handleKeyPress}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-right"
                    placeholder="نام کاربری خود را وارد کنید"
                    autoComplete="username"
                />
              </div>

              <div>
                <label className="block text-gray-700 mb-2 font-medium text-right">رمز عبور</label>
                <input
                    type="password"
                    value={loginData.password}
                    onChange={(e) => setLoginData({ ...loginData, password: e.target.value })}
                    onKeyPress={handleKeyPress}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-right"
                    placeholder="رمز عبور خود را وارد کنید"
                    autoComplete="current-password"
                />
              </div>

              <button
                  onClick={handleLogin}
                  className="w-full bg-indigo-600 text-white py-3 rounded-lg hover:bg-indigo-700 transition-colors font-medium shadow-md hover:shadow-lg"
              >
                ورود به سیستم
              </button>
            </div>

            <div className="mt-6 text-center text-sm text-gray-600">
              <p>نکته: برای تست می‌توانید یک کاربر جدید ثبت‌نام کنید</p>
            </div>
          </div>
        </div>
    );
  }

  return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-4xl mx-auto">
          <div className="bg-white rounded-2xl shadow-lg p-8">
            <div className="flex items-center justify-between mb-8">
              <h1 className="text-3xl font-bold text-gray-800">ایجاد فاکتور جدید</h1>
              <button
                  onClick={() => {
                    setCurrentPage('login');
                    setToken(null);
                    setError('');
                    setSuccess('');
                  }}
                  className="text-gray-600 hover:text-gray-800 px-4 py-2 rounded-lg hover:bg-gray-100 transition-colors"
              >
                خروج
              </button>
            </div>

            {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 flex items-center gap-2">
                  <AlertCircle className="w-5 h-5 flex-shrink-0" />
                  <span className="text-sm">{error}</span>
                </div>
            )}

            {success && (
                <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-4">
                  {success}
                </div>
            )}

            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-gray-700 mb-2 font-medium text-right">عنوان فاکتور *</label>
                  <input
                      type="text"
                      value={invoiceData.title}
                      onChange={(e) => setInvoiceData({ ...invoiceData, title: e.target.value })}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 text-right"
                      placeholder="مثال: فاکتور فروش"
                  />
                </div>

                <div>
                  <label className="block text-gray-700 mb-2 font-medium text-right">تاریخ فاکتور *</label>
                  <input
                      type="date"
                      value={invoiceData.invoiceDate}
                      onChange={(e) => setInvoiceData({ ...invoiceData, invoiceDate: e.target.value })}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-gray-700 mb-2 font-medium text-right">لوگو (اختیاری)</label>
                <div className="flex items-center gap-4">
                  <label className="flex items-center gap-2 px-4 py-2 bg-gray-100 rounded-lg cursor-pointer hover:bg-gray-200 transition-colors">
                    <Upload className="w-5 h-5" />
                    <span>انتخاب فایل</span>
                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleLogoUpload}
                        className="hidden"
                    />
                  </label>
                  {invoiceData.logo && (
                      <span className="text-sm text-gray-600">{invoiceData.logo.name}</span>
                  )}
                </div>
              </div>

              <div>
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-xl font-bold text-gray-800">اقلام فاکتور *</h2>
                  <button
                      onClick={addItem}
                      className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors shadow-md"
                  >
                    <Plus className="w-5 h-5" />
                    افزودن قلم
                  </button>
                </div>

                <div className="space-y-4">
                  {invoiceData.items.map((item, index) => (
                      <div key={index} className="grid grid-cols-12 gap-4 items-end p-4 bg-gray-50 rounded-lg">
                        <div className="col-span-5">
                          <label className="block text-gray-700 mb-2 text-sm text-right">نام کالا/خدمات</label>
                          <input
                              type="text"
                              value={item.itemName}
                              onChange={(e) => updateItem(index, 'itemName', e.target.value)}
                              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 text-right"
                              placeholder="نام کالا"
                          />
                        </div>

                        <div className="col-span-2">
                          <label className="block text-gray-700 mb-2 text-sm text-right">تعداد</label>
                          <input
                              type="number"
                              value={item.quantity}
                              onChange={(e) => updateItem(index, 'quantity', e.target.value)}
                              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 text-right"
                              min="1"
                          />
                        </div>

                        <div className="col-span-3">
                          <label className="block text-gray-700 mb-2 text-sm text-right">قیمت واحد (ریال)</label>
                          <input
                              type="number"
                              value={item.unitPrice}
                              onChange={(e) => updateItem(index, 'unitPrice', e.target.value)}
                              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 text-right"
                              min="0"
                              step="1000"
                          />
                        </div>

                        <div className="col-span-2">
                          <button
                              onClick={() => removeItem(index)}
                              className="w-full px-3 py-2 bg-red-100 text-red-600 rounded-lg hover:bg-red-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                              disabled={invoiceData.items.length === 1}
                              title={invoiceData.items.length === 1 ? 'حداقل یک قلم باید وجود داشته باشد' : 'حذف قلم'}
                          >
                            <Trash2 className="w-5 h-5 mx-auto" />
                          </button>
                        </div>
                      </div>
                  ))}
                </div>
              </div>

              <div className="border-t pt-4">
                <div className="flex justify-between items-center text-xl font-bold bg-indigo-50 p-4 rounded-lg">
                  <span>جمع کل:</span>
                  <span className="text-indigo-600">{calculateTotal()} ریال</span>
                </div>
              </div>

              <button
                  onClick={handleSubmitInvoice}
                  className="w-full flex items-center justify-center gap-2 bg-green-600 text-white py-3 rounded-lg hover:bg-green-700 transition-colors font-medium shadow-md hover:shadow-lg"
              >
                <Download className="w-5 h-5" />
                ایجاد و دانلود PDF
              </button>
            </div>
          </div>
        </div>
      </div>
  );
}

export default App;